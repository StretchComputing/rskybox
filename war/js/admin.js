var USERS_PATH = '/users';
var MOBILE_CARRIERS_PATH = '/mobileCarriers';

//
// List
//

// this is currently specific to /users, but that's all we have to admin right now
$('#index').live('pageshow', function() {
  var restUrl = REST_PREFIX + USERS_PATH;

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


//
// Item
//

var ITEM_PAGE = '#item';
var NEW_ITEM = 'new';

// set up the page(s) we need to build dynamically
dynamicPages([{
  page: ITEM_PAGE,
  'function': itemPage
}]);

$(ITEM_PAGE).live('pagecreate', function() {
  $(this).find('form').submit(saveItem);

  $('#sendSmsNotifications').change(function() {
    enableSmsDetails($('#sendSmsNotifications').prop('checked'));
  });
});

function itemPage(page, url) {
  var id = getParameterByName(url, 'id');
  var restUrl = REST_PREFIX + MOBILE_CARRIERS_PATH;

  page.find('form')[0].reset();
  jsonPopulate(restUrl, $('#mobileCarrierId'), function(select, carriers) {
    select.html(carrierOptions(carriers['mobileCarriers']));
    if (id === NEW_ITEM) {
      buildNewItemPage();
    } else {
      restUrl = REST_PREFIX + USERS_PATH + '/' + id;
      jsonPopulate(restUrl, page, buildItemPage);
    }
  });
}

function buildItemPage(page, item) {
  var smsEnabled = item['sendSmsNotifications'];

  pageHeader($(ITEM_PAGE)).find('h1').html('Update User');
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
  pageHeader($(ITEM_PAGE)).find('h1').html('Create User');
  $('#id').val(NEW_ITEM);
  $('#mobileCarrierId').val(NO_CARRIER).selectmenu('refresh');
  $('#sendEmailNotifications').prop('checked', false).checkboxradio('refresh');
  $('#sendSmsNotifications').prop('checked', false).checkboxradio('refresh');
  enableSmsDetails(false);
  $('#delete_button').hide();
}

function saveItem() {
  if (!validateUser()) { return false; }

  var restUrl = REST_PREFIX + USERS_PATH;
  var json = JSON.stringify({
    'firstName': $('#firstName').val(),
    'lastName': $('#lastName').val(),
    'emailAddress': $('#emailAddress').val(),
    'phoneNumber': $('#phoneNumber').val(),
    'mobileCarrierId': $('#mobileCarrierId').val(),
    'sendEmailNotifications': $('#sendEmailNotifications').prop('checked'),
    'sendSmsNotifications': $('#sendSmsNotifications').prop('checked')
  });

  if ($('#id').val() === NEW_ITEM) {
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

function validateUser() {
  if (!$('#firstName').val() || !$('#lastName').val() || !validEmailAddress($('#emailAddress').val())) {
    alert('You must enter a first name, last name, and valid email address.');
    return false;
  }
  if (!validateSms()) { return false; }
  return true;
}

$('#delete').live('pagecreate', function() {
  $('#delete_item').click(function(event) {
    var restUrl = REST_PREFIX + USERS_PATH + '/' + $('#id').val();

    deleteJson(restUrl, null, function() {
      event.preventDefault();
      $.mobile.changePage('#index');
    });
  });
});
