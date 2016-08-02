# EscherConverter

module.exports = config:
  paths:
    watched: ['EscherConverter']

  plugins:
    autoReload:
      enabled: yes
    coffeelint:
      pattern: /^EscherConverter\/.*\.(coffee)$/
      useCoffeelintJson: yes
    jaded:
      staticPatterns: /^EscherConverter\/markup\/([\d\w]*)\.jade$/
    postcss:
      processors: [
        require('autoprefixer')(['last 8 versions'])
      ]
    stylus:
      plugins: [ ]

  npm:
    enabled: yes
    styles:
      'picnic': [
        'releases/picnic.min.css'
      ]

  modules:
    nameCleaner: (path) ->
      path
        .replace /^EscherConverter\//, ''
        .replace /\.coffee/, ''

  files:
    javascripts:
      joinTo:
        'js/libraries.js': /^(?!EscherConverter\/)/
        'js/app.js': /^EscherConverter\//
    stylesheets:
      joinTo:
        'css/libraries.css': /^(?!EscherConverter\/)/
        'css/app.css': /^EscherConverter\//
