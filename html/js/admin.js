// this is currently specific to /users, but that's all we have to admin right now
$('#index').live('pageshow', function() {
  var restUrl = REST_PREFIX + '/users';

  jsonPopulate(restUrl, $('#index'), buildListPage);
});

function buildListPage(page, list) {
  var markup ='<ul data-role="listview">';
  var users = list['users'];

  for (i = 0; i < users.length; i++) {
    var user = users[i];
    var display = user['firstName'] + ' ' + user['lastName'] + ': ' + user['emailAddress'];
    markup += '<li><a href="#item?id=' + user['id'] +'">' + display +'</a></li>';
  }
  markup += '</ul>'
  pageContent(page, markup).find(':jqmData(role=listview)').listview();
}


// set up the page(s) we need to build dynamically
dynamicPages([{
  page: '#item',
  'function': itemPage
}]);

$('#item').live('pagecreate', function() {
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

function itemPage(page, url) {
  var id = getParameterByName(url, 'id');
  var restUrl = REST_PREFIX + '/mobileCarriers';

  
  page.find('form')[0].reset();
  jsonPopulate(restUrl, $('#mobileCarrierId'), function(select, carriers) {
    select.html(carrierOptions(carriers['mobileCarriers']));
    if (id == 'new') {
      $('#id').val('');
      $('#sendEmailNotifications').prop('checked', false).checkboxradio('refresh');
      $('#sendSmsNotifications').prop('checked', false).checkboxradio('refresh');
    } else {
      restUrl = REST_PREFIX + '/users/' + id;
      jsonPopulate(restUrl, page, buildItemPage);
    }
  });
}

function buildItemPage(page, item) {
  var smsEnabled = item['sendSmsNotifications'];

  $('#id').val(item['id']);
  $('#firstName').val(item['firstName']);
  $('#lastName').val(item['lastName']);
  $('#emailAddress').val(item['emailAddress']);

  $('#sendEmailNotifications').prop('checked', item['sendEmailNotifications']).checkboxradio('refresh');
  $('#sendSmsNotifications').prop('checked', smsEnabled).checkboxradio('refresh');
  enableSmsDetails(smsEnabled);
  $('#phoneNumber').val(item['phoneNumber']);
  $('#mobileCarrierId').val(item['mobileCarrierId']);

  page.page();
  page.trigger('create');
}

function switchPage(url, options) {
  var pageSelector = url.hash.replace(/\?.*$/, '');
  var page = $(pageSelector);

  enableSmsDetails($('#sendSmsNotifications').prop('checked'));
  options.dataUrl = url.href;
  $.mobile.changePage(page, options);
  $.mobile.hidePageLoadingMsg();
}

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
