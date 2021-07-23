// Complete JS code.
console.log('start');

var BASE_URL = "http://139.59.28.97/api";
var $ = require("cash-dom");
var nanoajax = require("nanoajax");
var saveAs = require('file-saver').saveAs;
var file_row = $('.file')[0].cloneNode(true);
var log_loaded = false;
var current_job = {};

var App = {
  init: function () {
    console.log('init!');
    $('.file').remove();
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
  $(row).find('.file-select-button').text(fileNumber);
  nanoajax.ajax({
    url: `${BASE_URL}/convert/${conversionId}/input/${fileNumber}`,
    method: 'HEAD'
  }, function (statusCode, responseText, request) {
    if (statusCode === 404) {
      switch (job_status) {
        case 'started':
        case 'waiting':
          $(row).find('.file-format > span').removeClass();
          $(row).find('.file-format > span').addClass('label');
          $(row).find('.file-format > span').html('none');
          $(row).find('.file-input-status > span').removeClass();
          $(row).find('.file-input-status > span').addClass('label warning');
          $(row).find('.file-input-status > span').html('waiting');
          $(row).find('.file-output-status > span').removeClass();
          $(row).find('.file-output-status > span').addClass('label normal');
          $(row).find('.file-output-status > span').html('not started');
          var inputUploadButton = $(row).find('.file-input-upload > button')[0];
          $(inputUploadButton).attr('data-url', `${BASE_URL}/convert/${conversionId}/input/${fileNumber}`);
          $(inputUploadButton).on('click', function () {
            return uploadFile.call(undefined, row, inputUploadButton);
          });
          $(row).find('.file-input-download > button').attr('disabled', true);
          $(row).find('.file-output-download > button').attr('disabled', true);
          break;
        case 'failed':
        case 'errored':
          $(row).find('.file-format > span').removeClass();
          $(row).find('.file-format > span').addClass('label');
          $(row).find('.file-format > span').html('none');
          $(row).find('.file-input-status > span').removeClass();
          $(row).find('.file-input-status > span').addClass('label error');
          $(row).find('.file-input-status > span').html('not available');
          $(row).find('.file-output-status > span').removeClass();
          $(row).find('.file-output-status > span').addClass('label error');
          $(row).find('.file-output-status > span').html('failed');
          var inputUploadButton = $(row).find('.file-input-upload > button')[0];
          $(inputUploadButton).attr('data-url', `${BASE_URL}/convert/${conversionId}/input/${fileNumber}`);
          $(inputUploadButton).attr('disabled', true);
          $(row).find('.file-input-download > button').attr('disabled', true);
          $(row).find('.file-output-download > button').attr('disabled', true);
          break;
      }
    }
    else if (statusCode/100 == 2) {
      $(row).find('.file-format > span').removeClass();
      $(row).find('.file-format > span').addClass('label');
      $(row).find('.file-format > span').html(request.getResponseHeader('Content-Type'));
      $(row).find('.file-input-status > span').removeClass();
      $(row).find('.file-input-status > span').addClass('label success');
      $(row).find('.file-input-status > span').html('uploaded');
      switch (job_status) {
        case 'started':
        case 'waiting':
          $(row).find('.file-output-status > span').removeClass();
          $(row).find('.file-output-status > span').addClass('label normal');
          $(row).find('.file-output-status > span').html('not started');
          var inputUploadButton = $(row).find('.file-input-upload > button')[0];
          $(inputUploadButton).attr('data-url', `${BASE_URL}/convert/${conversionId}/input/${fileNumber}`);
          $(inputUploadButton).on('click', function () {
            return uploadFile.call(undefined, row, inputUploadButton);
          });
          $(row).find('.file-input-download > button').attr('data-url',
              `${BASE_URL}/convert/${conversionId}/input/${fileNumber}`);
          $(row).find('.file-input-download > button').on('click', function () {
            return downloadFile.call(undefined, row, $(row).find('.file-input-download > button'));
          });
          $(row).find('.file-input-download > button').attr('disabled', true);
          $(row).find('.file-output-download > button').attr('disabled', true);
          break;
        case 'failed':
        case 'errored':
          $(row).find('.file-output-status > span').removeClass();
          $(row).find('.file-output-status > span').addClass('label error');
          $(row).find('.file-output-status > span').html('failed');
          $(row).find('.file-input-upload > button').attr('disabled', true);
          $(row).find('.file-input-download > button').removeAttr('disabled');
          $(row).find('.file-input-download > button').attr('data-url',
            `${BASE_URL}/convert/${conversionId}/input/${fileNumber}`);
          $(row).find('.file-input-download > button').on('click', function () {
            return downloadFile.call(undefined, row, $(row).find('.file-input-download > button'));
          });
          $(row).find('.file-output-download').attr('disabled', true);
          break;
        case 'running':
          $(row).find('.file-output-status > span').removeClass();
          $(row).find('.file-output-status > span').addClass('label warning');
          $(row).find('.file-output-status > span').html('running');
          $(row).find('.file-input-upload > button').attr('disabled', true);
          $(row).find('.file-input-download > button').removeAttr('disabled');
          $(row).find('.file-input-download > button').attr('data-url',
              `${BASE_URL}/convert/${conversionId}/input/${fileNumber}`);
          $(row).find('.file-input-download > button').on('click', function () {
            return downloadFile.call(undefined, row, $(row).find('.file-input-download > button'));
          });
          $(row).find('.file-output-download').attr('disabled', true);
          break;
        case 'completed':
          $(row).find('.file-output-status > span').removeClass();
          $(row).find('.file-output-status > span').addClass('label success');
          $(row).find('.file-output-status > span').html('converted');
          $(row).find('.file-input-upload > button').attr('disabled', true);
          $(row).find('.file-input-download > button').removeAttr('disabled');
          $(row).find('.file-input-download > button').attr('data-url',
              `${BASE_URL}/convert/${conversionId}/input/${fileNumber}`);
          $(row).find('.file-input-download > button').on('click', function () {
            return downloadFile.call(undefined, row, $(row).find('.file-input-download > button'));
          });
          $(row).find('.file-output-download > button').attr('data-url',
              `${BASE_URL}/convert/${conversionId}/output/${fileNumber}`);
          $(row).find('.file-output-download > button').on('click', function () {
            return downloadFile.call(undefined, row, $(row).find('.file-output-download > button'));
          });
          break;
      }
    }
  });
  return row;
}

function downloadFile(row, button) {
  nanoajax.ajax({
    method: 'GET',
    url: $(button).data('url')
  }, function (fileDownloadStatus, fileDownloadText, request) {
    if (Math.floor(fileDownloadStatus/100) == 2) {
      var extension = request.getResponseHeader('Content-Type').split("/")[1];
      var id = $('#conversion-number-input').val();
      var number = $(row).find('.file-select-button').text();
      var file = new File([fileDownloadText], `${id}_${number}.${extension}`, {type: "text/plain;charset=utf-8"});
      saveAs(file);
    }
  });
}

function uploadFile (row, button) {
  $(button).attr('disabled', true);
  var file = $(row).find('.file-input-select > label > input')[0].files[0];
  console.log(file);
  if (file) {
    var reader = new FileReader();
    reader.readAsText(file);
    nanoajax.ajax({
      method: 'PUT',
      url: $(button).data('url'),
      body: $(row).find('.file-input-select > label > input')[0].target,
      headers: {
        'Content-Type': $(row).find('.file-format-select > select').val()
      }
    }, function (fileUploadStatusCode, fileUploadResponseText) {
      if (Math.floor(fileUploadStatusCode/100) == 2) {
        $(row).find('.file-input-status > span').removeClass();
        $(row).find('.file-input-status > span').addClass('label success');
        $(row).find('.file-input-status > span').text('uploaded');
        $(row).find('.file-format > span').removeClass();
        $(row).find('.file-format > span').addClass('label');
        $(row).find('.file-format > span').text($(row).find('.file-format-select > select').val());
      }
      $(button).removeAttr('disabled');
    });
  }
}

function startNewConversion() {
  var req = {};
  req.output_format = $('#output-format-container').find('select').val().toLowerCase();
  req.file_count = $('.file').length;

  nanoajax.ajax({
    method: 'POST',
    url: `${BASE_URL}/convert`,
    headers: {
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(req)
  }, function (statusCode, responseText, request) {
    if (Math.floor(statusCode/100) == 2) {
      var obj = JSON.parse(responseText);
      $('#conversion-number-input').val(obj.id);
      $('#conversion-number-input').attr('disabled', true);
      $('.file').each(function (row, i, array) {
        $(row).find('.file-input-upload > button').attr('data-url',
          `${BASE_URL}/convert/${obj.id}/input/${i}`);
        uploadFile(row, $(row).find('.file-input-upload > button'));
      })
    }
  });
}

function searchConversionId() {
  var id = $('#conversion-number-input').val();
  console.log('Converion ID:', id);
  if (!id) {
    // alert('Please enter a valid request id...');
  }
  else {
    // Get request info.
    $('#files').find('div.file').remove();
    nanoajax.ajax({
      url: BASE_URL + '/convert/' + id
    }, function (code, responseText) {
      console.log(responseText);
      updateStatus();
      if (code == 200) {
        var resp = JSON.parse(responseText);
        var file_rows = [];
        // Add rows of files.
        for (var i = 0; i < resp.total_files ; i++) {
          file_rows.push(prepareFileRow(id, i, resp.status));
        }
        $(file_rows).each(function (item) {
          $('#files')[0].appendChild(item);
        });
        console.log(file_rows);
      }
    });
  }
}


$('#conversion-number-button').on('click', searchConversionId);


// Clear all and prepare for a new conversion.
$('#conversion-number-new').on('click', cleanWorkspace);


// Add new file row.
$('#add-file-button').on('click', function () {
  var r = file_row.cloneNode(true);
  $(r).find('.file-select-button').text($('.file').length);
  $('#files').append(r);
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


$('#start-upload-button').on('click', startNewConversion);

module.exports = App;
