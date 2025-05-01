# daily.py
from flask import Blueprint, request, jsonify
from pytrends.request import TrendReq
import datetime, traceback, calendar
import pandas as pd

daily_bp = Blueprint('daily', __name__, url_prefix='/daily')

@daily_bp.route('', methods=['GET'])
def daily_trend():
    kw = request.args.get('keyword', '').strip()
    if not kw:
        return jsonify([]), 200

    # year/month 파라미터 받기 (없으면 기본: 오늘 기준 지난 1년)
    year_param  = request.args.get('year')
    month_param = request.args.get('month')
    today = datetime.date.today()

    if year_param and month_param:
        try:
            year = int(year_param)
            month = int(month_param)
            # 해당 월의 첫날~마지막날 계산
            start_date = datetime.date(year, month, 1)
            last_day   = calendar.monthrange(year, month)[1]
            end_date   = datetime.date(year, month, last_day)
        except ValueError:
            return jsonify({'error': 'year 혹은 month 파라미터가 올바르지 않습니다.'}), 400
    else:
        # 기본 동작: 오늘 기준 365일 전부터 오늘까지
        end_date   = today
        start_date = today - datetime.timedelta(days=365)

    try:
        pytrends = TrendReq(hl='en-US', tz=360)
        # timeframe은 'YYYY-MM-DD YYYY-MM-DD'
        timeframe = f"{start_date.isoformat()} {end_date.isoformat()}"
        pytrends.build_payload([kw], timeframe=timeframe, cat=0)

        df = pytrends.interest_over_time()
        if df.empty or kw not in df.columns:
            return jsonify([]), 200

        # 일별 검색량 JSON 변환
        result = []
        for date, row in df.iterrows():
            result.append({
                'date': date.strftime('%Y-%m-%d'),
                'searchVolume': float(row[kw] or 0.0)
            })
        return jsonify(result), 200

    except Exception:
        traceback.print_exc()
        return jsonify({'error': 'failed to fetch daily data'}), 500
