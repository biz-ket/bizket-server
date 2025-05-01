#app.py
from flask import Flask
from flask_cors import CORS
from related_suggest   import related_bp
from daily import daily_bp
from monthly import monthly_bp

import os


app = Flask(__name__)
app.config['JSON_AS_ASCII'] = False
CORS(app)

app.register_blueprint(related_bp)   # /related trending
app.register_blueprint(daily_bp)
app.register_blueprint(monthly_bp)

if __name__ == '__main__':
    # 환경변수 PY_SERVICE_PORT 없으면 5000
    port = int(os.environ.get('PY_SERVICE_PORT', 5000))
    app.run(host='0.0.0.0', port=port)
