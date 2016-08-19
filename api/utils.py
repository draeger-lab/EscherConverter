

def extension_from_content_type():
    """
    Returns the appropriate extension for the given Content-Type.
    
    :return: The file extension.
    """
    return {
        'application/xml': '.xml',
        'text/xml': '.xml',
        'application/atom+xml': '.xml',
        'application/json': '.json'
        'text/json'
    }
