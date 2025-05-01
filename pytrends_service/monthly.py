# monthly.py
from flask import Blueprint, request, jsonify
from pytrends.request import TrendReq
import datetime

monthly_bp = Blueprint('monthly', __name__, url_prefix='/monthly')

@monthly_bp.route('', methods=['GET'])
def monthly_trend():
    kw = request.args.get('keyword', '').strip()
    if not kw:
        return jsonify([]), 200

    # pytrends 초기화
    pytrends = TrendReq(hl='en-US', tz=360)
    # 최근 12개월 monthly 데이터
    pytrends.build_payload([kw], timeframe='today 12-m', cat=0)
    df = pytrends.interest_over_time()

    # 월별 평균으로 리샘플링
    monthly = df.resample('M').mean()
    result = []
    for date, row in monthly.iterrows():
        result.append({
            'keyword':      kw,
            'yearMonth':    date.strftime('%Y-%m'),
            'searchVolume': float(row.get(kw, 0.0))
        })
    return jsonify(result), 200
