##### 주의!!!!! 한국어 키워드 불가
##### naver, bing등 시도해보았지만 결국 유료서비스
import requests
from flask import Blueprint, request, jsonify

related_bp = Blueprint('related', __name__, url_prefix='/related')

@related_bp.route('', methods=['GET'])
def related_suggest():
    kw = request.args.get('keyword', '').strip()
    if not kw:
        return jsonify({'top': [], 'rising': []}), 200

    try:
        resp = requests.get(
            "https://suggestqueries.google.com/complete/search",
            params={
                'client': 'firefox',
                'hl': 'en',
                'gl': 'US',
                'q': kw
            },
            headers={'User-Agent': 'Mozilla/5.0'},
            timeout=3
        )
        resp.raise_for_status()
        data = resp.json()
        suggestions = data[1] if len(data) > 1 else []
    except Exception:
        suggestions = []

    top10 = suggestions[:10]
    return jsonify({'top': top10, 'rising': []}), 200
