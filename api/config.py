import os

config = {
    'PORT': os.getenv('ESCHER_CONVERTER_PORT', 6969),
    'HOST': os.getenv('ESCHER_CONVERTER_HOST', '0.0.0.0'),
    'SQLITE_FILE': os.getenv('ESCHER_CONVERTER_DB_FILE', '/temp/escher.db'),
    'JAR_PATH': os.getenv('ESCHER_CONVERTER_PATH', '/temp/EscherConverter.jar'),
    'FILE_STORE': os.getenv('ESCHER_CONVERTER_FILE_STORE', '/temp/escher_converter/files/'),
    'DEBUG': True,
    'TEST_CONFIG': {
        'SQLITE_FILE': '/temp/test/escher.db'
    }
}
