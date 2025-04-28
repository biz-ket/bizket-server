from flask import Flask, request, jsonify
from flask_cors import CORS
from pytrends.request import TrendReq
import os

app = Flask(__name__)
CORS(app)

pytrends = TrendReq(hl='en-US', tz=0)

@app.route('/ping')
def ping():
    return 'pong', 200

@app.route('/monthly')
def monthly():
    kw = request.args.get('keyword')
    if not kw:
        return 'keyword query param required', 400

    try:
        # 지난 12개월의 관심 지표
        # 검색 횟수는 유료 - G.Ads keyword planner api
        pytrends.build_payload([kw], cat=0, timeframe='today 12-m', geo='')
        df = pytrends.interest_over_time()
        # 빈 데이터프레임인 경우
        if df.empty:
            return jsonify([])

        result = []
        for dt, row in df.iterrows():
            result.append({
                'keyword': kw,
                'yearMonth': dt.strftime('%Y-%m'),
                'searchVolume': int(row[kw])
            })
        return jsonify(result)
    except Exception as e:
        return str(e), 500

if __name__ == '__main__':
    port = int(os.environ.get('PY_SERVICE_PORT', 5000))
    app.run(host='0.0.0.0', port=port)
