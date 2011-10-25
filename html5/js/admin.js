var ITEM_PATH = '/' + ITEM_NAME;
var MOBILE_CARRIERS_PATH = '/mobileCarriers';

//
// List
//

$('#index').live('pageshow', function() {
  var restUrl = REST_PREFIX + ITEM_PATH;

  jsonPopulate(restUrl, $('#index'), buildListPage);
});

function buildListPage(page, list) {
  var markup ='<ul data-role="listview">';
  var items = list[ITEM_NAME];

  for (i = 0; i < items.length; i++) {
    var item = items[i];
    markup += '<li><a href="#item?id=' + item.id +'">' + getItemLinkText(item) +'</a></li>';
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

function saveItem() {
  if (!validateUser()) { return false; }

  var restUrl = REST_PREFIX + ITEM_PATH;
  var json = JSON.stringify({
    'firstName': $('#firstName').val(),
    'lastName': $('#lastName').val(),
    'emailAddress': $('#emailAddress').val(),
    'phoneNumber': $('#phoneNumber').val(),
    'mobileCarrierId': $('#mobileCarrierId').val(),
    'sendEmailNotifications': $('#sendEmailNotifications').prop('checked'),
    'sendSmsNotifications': $('#sendSmsNotifications').prop('checked')
  });

  if ($('#id').val() !== NEW_ITEM) {
    restUrl += '/' + $('#id').val();
  }
  putJson(restUrl, json, function() {
    history.back();
  });
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
    var restUrl = REST_PREFIX + ITEM_PATH + '/' + $('#id').val();

    deleteJson(restUrl, null, function() {
      event.preventDefault();
      $.mobile.changePage('#index');
    });
  });
});
