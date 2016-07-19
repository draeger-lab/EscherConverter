import os

config = {
    'PORT': os.getenv('ESCHER_CONVERTER_PORT', 6969),
    'HOST': os.getenv('ESCHER_CONVERTER_HOST', '0.0.0.0'),
    'SQLITE_FILE': os.getenv('ESCHER_CONVERTER_DB_FILE', '/temp/'),
    'DEBUG': True
}
