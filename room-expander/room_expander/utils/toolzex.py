def prop(key):
    return lambda obj: obj[key] if isinstance(obj, dict) else getattr(obj, key)


def or_else(default):
    return lambda obj: obj or default
