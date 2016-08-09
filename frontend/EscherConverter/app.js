// Complete JS code.
console.log('start');

var BASE_URL = "http://localhost:6969/api";
var $ = require("cash-dom");
var nanoajax = require("nanoajax");
var saveAs = require('file-saver').saveAs;
var file_row = $('.file')[0].cloneNode(true);
var log_loaded = false;
var current_job = {};

var App = {
  init: function () {
    console.log('init!');
    $('.file').each(function (f) {
      f.style.display = 'none';
    })
  }
};

function cleanWorkspace() {
  $('#files').find('div.file').remove();
  $('#conversion-status-span').removeClass();
  $('#conversion-status-span').addClass('label');
  $('#conversion-status-span').val('status');
  $('#conversion-number-input').val('');
  $('pre#conversion-log').text('');
  $('pre#conversion-log').attr('hidden', true);
}

function prepareFileRow(conversionId, fileNumber, job_status) {
  var row = file_row.cloneNode(true);
  $(row).find('.file-select-button').val(fileNumber);
  nanoajax.ajax({
    url: `${BASE_URL}/convert/${conversionId}/input/${fileNumber}`,
    method: 'HEAD'
  }, function (statusCode, responseText) {
    if (statusCode === 200) {
      switch (job_status) {
        case 'started':
        case 'waiting':
          $(row).find('.file-format > span').removeClass();
          $(row).find('.file-format > span').addClass('label warning');
          $(row).find('.file-format > span').text('waiting');
          $(row).find('.file-input-status > span').removeClass();
          $(row).find('.file-input-status > span').addClass('label warning');
          $(row).find('.file-input-status > span').text('waiting');
          $(row).find('.file-output-status > span').removeClass();
          $(row).find('.file-output-status > span').addClass('label normal');
          $(row).find('.file-output-status > span').text('not started');
          $(row).find('.file-input-upload > button').attr('data-url',
              `${BASE_URL}/convert/${conversionId}/input/${fileNumber}`);
          $(row).find('.file-input-upload > button').on('click', uploadFile(row));
          $(row).find('.file-input-download').attr('disabled', true);
          $(row).find('.file-output-download').attr('disabled', true);
          break;
      }
    }
  });
  return row;
}

function uploadFile (row) {
  this.disable();
  nanoajax.ajax({
    method: 'GET',
    url: $(row).find('.file-input-select > input').val(),
    headers: {
      'Content-Type': $(row).find('.file-format-select > select').val()
    }
  }, function (fileStatusCode, fileResponseText) {
    if (fileStatusCode == 200) {
      nanoajax.ajax({
        method: 'PUT',
        url: $(this).data('url'),
        body: fileResponseText
      }, function (fileUploadStatusCode, fileUploadResponseText) {
        if (fileUploadStatusCode == 200) {
          $(row).find('.file-input-status > span').removeClass();
          $(row).find('.file-input-status > span').addClass('label success');
          $(row).find('.file-input-status > span').text('uploaded');
          $(row).find('.file-format > span').removeClass();
          $(row).find('.file-format > span').addClass('label');
          $(row).find('.file-format > span').text('waiting');
        }
      })
    }
  });
}

$('#conversion-number-button').on('click', function () {
  var id = $('#conversion-number-input').val();
  console.log('Converion ID:', id);
  if (!id) {
    // alert('Please enter a valid request id...');
  }
  else {
    // Get request info.
    nanoajax.ajax({
      url: BASE_URL + '/convert/' + id
    }, function (code, responseText) {
      console.log(responseText);
      if (code == 404) {
        $('#conversion-status-span').text('not found');
        $('#conversion-status-span').addClass('error');
        $('#conversion-number-input').val("");
      }
      else if (code == 200) {
        var resp = JSON.parse(responseText);
        var file_rows = [];
        // Add rows of files.
        for (var i = 0; i < resp.total_files ; i++) {
          file_rows.push(prepareFileRow(id, i, resp.status));

          /* Old code for preparing a file row.
          var file_row = $('.file')[0].cloneNode(true);
          $(file_row).find('.file-number')[0].val('i+1');
          // Make head request for each file.
          nanoajax.ajax({
            method: 'HEAD',
            url: BASE_URL + '/convert/' + id + '/input/' + i
          }, function (code, responseText) {
            if (code === 404) {
              switch (resp.status) {
                case "started":
                case "waiting":
                  // TODO: Add upload functionality.
                  $(file_row).find('span')[0].removeClass();
                  $(file_row).find('span')[0].addClass('label waiting');
                  $(file_row).find('span')[0].text('not uploaded yet');
                  break;
                case "running":
                  $(file_row).find('span')[0].removeClass();
                  $(file_row).find('span')[0].addClass('label waiting');
                  $(file_row).find('span')[0].text('conversion running');
                  break;
                case "failed":
                  $(file_row).find('span')[0].removeClass();
                  $(file_row).find('span')[0].addClass('label error');
                  $(file_row).find('span')[0].text('conversion failed');
                  break;
                case "completed":
                  $(file_row).find('span')[0].removeClass();
                  $(file_row).find('span')[0].addClass('label success');
                  $(file_row).find('span').text('converted successfully');
                  break;
                case "errored":
                  $(file_row).find('span')[0].removeClass();
                  $(file_row).find('span')[0].addClass('label error');
                  $(file_row).find('.file-status')[0].val('conversion failed');
                  break;
              }

            }
          });
          */
        }
        // $('#files')[0].appendChild(file_rows);
        $(file_rows).each(function (item) {
          $('#files')[0].appendChild(item);
        })
      }
    });
  }
});


// Clear all and prepare for a new conversion.
$('#conversion-number-new').on('click', cleanWorkspace);


// Add new file row.
$('#add-file-button').on('click', function () {
  $('#files').append(file_row.cloneNode(true));
});


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

// Update job status.
function updateStatus() {
  var idInput = $('#conversion-number-input');
  var id = $('#conversion-number-input').val();
  var statusSpan = $('#conversion-status-span');
  statusSpan.removeClass();
  statusSpan.addClass('label');
  if (id) {
    nanoajax.ajax({
    method: 'GET',
    url: `${BASE_URL}/convert/${id}`
  }, function (code, response) {
      var resp = JSON.parse(response);
      if (code == 404) {
        statusSpan.addClass('error');
        statusSpan.text(`not found: ${id}`);
        idInput.val("");
      }
      if (code == 200) {
        switch (resp.status) {
          case 'started':
          case 'waiting':
            statusSpan.addClass('warning');
            statusSpan.text('waiting');
            break;
          case 'running':
            statusSpan.addClass('warning');
            statusSpan.text('running');
            break;
          case 'failed':
          case 'errored':
            statusSpan.addClass('error');
            statusSpan.text(resp.status);
            break;
          case 'completed':
            statusSpan.addClass('success');
            statusSpan.text('completed');
            break;
        }
      }
    });
  }
  else {
    statusSpan.text('status');
  }
}


$('#conversion-status-refresh').on('click', updateStatus);

module.exports = App;
