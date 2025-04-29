#region.py
from flask import Blueprint, request, jsonify
from pytrends.request import TrendReq

region_bp = Blueprint('region', __name__)
#한국에 대한 트랜드만 분석함
pytrends = TrendReq(hl='ko', tz=540)

@region_bp.route('/region')
def by_region():
    kw = request.args.get('keyword')
    geo = request.args.get('geo', '')
    # 국가 코드(e.g. 'US', ''=전체)
    # 'CITY', 'DMA', 'REGION', 'COUNTRY' 등 사용 가능
    resolution = request.args.get('resolution', 'COUNTRY')

    if not kw:
        return 'keyword query param required', 400

    try:

        pytrends.build_payload([kw], cat=0, timeframe='today 12-m', geo=geo)
        df = pytrends.interest_by_region(resolution=resolution)
        if df.empty:
            return jsonify([])

        result = []
        for region_name, row in df.iterrows():
            result.append({
                'keyword':  kw,
                'region':   region_name,
                'interest': float(row[kw])
            })
        return jsonify(result)
    except Exception as e:
        return str(e), 500

#curl -s "http://localhost:8080/trends/java/region?geo=KR&resolution=REGION" | jq
#trends/{kw}/... kw에 키워드 입력