// Complete JS code.
console.log('start');

var BASE_URL = "http://localhost:6969/api";
var $ = require("cash-dom");
var nanoajax = require("nanoajax");
var saveAs = require('file-saver').saveAs;
var file_row = $('.file')[0].cloneNode(true);
var log_loaded = false;

var App = {
  init: function () {
    console.log('init!');
  }
};


// Output log.
$('#download-log-file').on('click', function () {
  var id = $('#conversion-number-input').val();
  if (log_loaded) {
    // TODO: Save contents of output as file.
  }
  else {
    nanoajax.ajax({ url: BASE_URL + '/convert/' + id + '/log'}, function (code, responseText) {
      if (code/100 == 2) {
        var file = new File([responseText], id + "_log.txt", {type: "text/plain;charset=utf-8"});
        saveAs(file);
      }
      else {
        alert('Log file not available!!!');
      }
    });
  }
});

module.exports = App;
