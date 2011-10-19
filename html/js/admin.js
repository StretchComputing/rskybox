// set up the page(s) we need to build dynamically
dynamicPages([{
  page : 'item',
  'function' : itemPage
}]);

function showList(page, list) {
  var content = page.find(':jqmData(role=content)');
  var markup ='<ul data-role="listview">';
  for (i = 0; i < list.length; i++) {
    var user = list[i];
    var display = user['firstName'] + ' ' + user['lastName'] + ': ' + user['emailAddress'];
    markup += '<li><a href="#item?id=' + user['id'] +'">' + display +'</a></li>';
  }
  markup += '</ul>'
  content.html(markup);
  content.find(':jqmData(role=listview)').listview();
}

$('#index').live('pageshow', function() {
  $.mobile.showPageLoadingMsg();
  $.getJSON(REST_PREFIX + '/users', function(list) {
    showList($('#index'), list['users']);
    $.mobile.hidePageLoadingMsg();
  });
});

//   function saveOtherItem() {
//     var method, url;
//     if ($('#id').val().length > 0) {
//       method = 'PUT';
//       url = '/rest/users/' + $('#id').val();
//     } else {
//       method = 'POST';
//       url = '/rest/users';
//     }
//     var json = JSON.stringify($(this).serializeJSON());
//     $.ajax({
//       type : method,
//       url : url,
//       contentType : 'application/json',
//       data : json,
//       success : function(data) {
//         display_user(data);
//       },
//       dataType : 'json'
//     });
//     return false;
//   }

function saveItem() {
  if (!validateSms()) { return false; }

  var json = JSON.stringify({
    'firstName' : $('#firstName'),
    'lastName' : $('#lastName'),
    'emailAddress' : $('#emailAddress'),
    'phoneNumber' : $('#phoneNumber').val(),
    'mobileCarrierId' : $('#mobileCarrierId').val(),
    'sendEmailNotifications' : $('#sendEmailNotifications').prop('checked'),
    'sendSmsNotifications' : $('#sendSmsNotifications').prop('checked')
  });
  $.ajax({
    type : 'PUT',
    url : REST_PREFIX + '/users/' + $('#id').val(),
    contentType : 'application/json',
    data : json,
    success : function(data) {
      history.back();
    },
    dataType : 'json'
  });
  return false;
}


function itemPage(url, options) {
  $.mobile.showPageLoadingMsg();
  $('#item').find('form')[0].reset();
  var id = url.hash.replace(/.*id=/, '');
  $.getJSON(REST_PREFIX + '/mobileCarriers', function(carriers) {
    $('#mobileCarrierId').html(carrierOptions(carriers['mobileCarriers']));
    if (id == 'new') {
      switchPage(url, options);
    } else {
      $.getJSON(REST_PREFIX + '/users/' + id, function(item) {
        setupItem(item, url, options);
        switchPage(url, options);
      });
    }
  });
}

function setupItem(item) {
  $('#id').val(item['id']);
  $('#firstName').val(item['firstName']);
  $('#lastName').val(item['lastName']);
  $('#emailAddress').val(item['emailAddress']);

  $('#sendEmailNotifications').prop('checked', item['sendEmailNotifications']);
  $('#sendSmsNotifications').prop('checked', item['sendSmsNotifications']);
  $('#phoneNumber').val(item['phoneNumber']);
  $('#mobileCarrierId').val(item['mobileCarrierId']);
}

function switchPage(url, options) {
  var pageSelector = url.hash.replace(/\?.*$/, '');
  var page = $(pageSelector);

  enableSmsDetails($('#sendSmsNotifications').prop('checked'));
  page.page();
  page.trigger('create');

  options.dataUrl = url.href;
  $.mobile.changePage(page, options);
  $.mobile.hidePageLoadingMsg();
}

function enableSmsDetails(enabled) {
  $('#phoneNumber').prop('disabled', !enabled);
  $('#mobileCarrierId').prop('disabled', !enabled);
}

function validateSms() {
  if (!$('#sendSmsNotifications').prop('checked')) { return true; }

  if (!validPhoneNumber($('#phoneNumber').val())) {
    alert('Please enter a valid phone number.');
    return false;
  }
  if ($('#mobileCarrierId').val() == NO_CARRIER) {
    alert('Please select a mobile carrier.');
    return false;
  }

  return true;
}

$('#item').live('pagecreate', function() {
  console.log('pagecreate fired');
  $(this).find('form').submit(saveItem);
  $('#sendSmsNotifications').change(function() {
    enableSmsDetails($('#sendSmsNotifications').prop('checked'));
  });
});

$('#delete').live('pagecreate', function() {
  $('#delete_item').click(function() {
    $.ajax({
      type : 'DELETE',
      url : REST_PREFIX + '/users/' + $('#id').val(),
      contentType : 'application/json',
      success : function(data) {
        $.mobile.changePage('#index');
      },
      dataType : 'json'
    });
  });
});