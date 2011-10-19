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

function itemPage(page, url) {
  var id = getParameterByName(url, 'id');
  var restUrl = REST_PREFIX + '/mobileCarriers';

  page.find('form')[0].reset();
  jsonPopulate(restUrl, $('#mobileCarrierId'), function(select, carriers) {
    select.html(carrierOptions(carriers['mobileCarriers']));
    if (id == 'new') {
      buildNewItemPage();
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
  $('#mobileCarrierId').val(item['mobileCarrierId']).selectmenu('refresh');
  $('#delete_button').show();
}

function buildNewItemPage() {
  $('#id').val('new');
  $('#mobileCarrierId').val(NO_CARRIER).selectmenu('refresh');
  $('#sendEmailNotifications').prop('checked', false).checkboxradio('refresh');
  $('#sendSmsNotifications').prop('checked', false).checkboxradio('refresh');
  enableSmsDetails(false);
  $('#delete_button').hide();
}

function saveItem() {
  if (!validateSms()) { return false; }

  var restUrl = REST_PREFIX + '/users';
  var json = JSON.stringify({
    'firstName': $('#firstName').val(),
    'lastName': $('#lastName').val(),
    'emailAddress': $('#emailAddress').val(),
    'phoneNumber': $('#phoneNumber').val(),
    'mobileCarrierId': $('#mobileCarrierId').val(),
    'sendEmailNotifications': $('#sendEmailNotifications').prop('checked'),
    'sendSmsNotifications': $('#sendSmsNotifications').prop('checked')
  });

  if ($('#id').val() == 'new') {
    postJson(restUrl, json, function() {
      history.back();
    });
  } else {
    restUrl += '/' + $('#id').val();
    putJson(restUrl, json, function() {
      history.back();
    });
  }
  return false;
}

$('#delete').live('pagecreate', function() {
  $('#delete_item').click(function(event) {
    var restUrl = REST_PREFIX + '/users/' + $('#id').val();

    deleteJson(restUrl, null, function() {
      event.preventDefault();
      $.mobile.changePage('#index');
    });
  });
});
