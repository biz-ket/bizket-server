# monthly.py
from flask import Blueprint, request, jsonify
from pytrends.request import TrendReq

monthly_bp = Blueprint('monthly', __name__)
pytrends = TrendReq(hl='en-US', tz=0)

@monthly_bp.route('/monthly')
def monthly():
    kw = request.args.get('keyword')
    if not kw:
        return 'keyword query param required', 400

    try:
        pytrends.build_payload([kw], cat=0, timeframe='today 12-m', geo='')
        df = pytrends.interest_over_time()
        if df.empty:
            return jsonify([])

        result = []
        for dt, row in df.iterrows():
            result.append({
                'keyword':      kw,
                'yearMonth':    dt.strftime('%Y-%m'),
                'searchVolume': int(row[kw])
            })
        return jsonify(result)
    except Exception as e:
        return str(e), 500

# curl -s "http://localhost:8080/trends/java/monthly" | jq