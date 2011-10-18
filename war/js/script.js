// A place to easily update the version of rest we're using.
var REST_PREFIX = '/rest/v1/';


// Each type of item has its own index and archives page.
// The following blocks set up event handlers for these internal pages.
$('#index').live('pageshow', function() {
  listPage($('#index'));
});

$('#archives').live('pageshow', function() {
  listPage($('#archives'), 'archived');
});

// Retrieves and displays the appropriate list for the given page.
//
// page: the page we are setting up
// status (optional): default is 'new', but this parameter can override the default
function listPage(page, status) {
  var restUrl = REST_PREFIX + itemName() + (status ? '?status='+ status : '');

  jsonPage(restUrl, page, buildListPage)
}

// Generic function to build the list of items.
//
// Calls listItem which is defined per type of item in its HTML file.
// list: JSON object containing the list elements
function buildListPage(page, list) {
  var markup = '<ul data-role="listview">';

  for (i = 0; i < list[itemName()].length; i++) {
    var item = list[itemName()][i];
    var display = item['date'] + ' - ' + item['userName'] + ' - ' + item['instanceUrl'];
    markup += '<li><a href="#item?id=' + item['id'] + '">' + display + '</a></li>';
  }
  markup += '</ul>'
  pageContent(page, markup).find(':jqmData(role=listview)').listview();
}


// set up the page(s) we need to build dynamically
dynamicPages([{
  page : 'item',
  'function' : itemPage
}]);

// Generic function to show the item page.
//
// url: the URL object of the current page
// options: JQM options for the current page (is this a correct statement?)
function itemPage(url, options) {
  var changeStatus = getParameterByName(url, 'changeStatus');
  var restUrl = REST_PREFIX + itemName() + '/' + getParameterByName(url, 'id');
  // Get the page hash portion of the URL, make a jQuery element out of it.
  var page = $(getPageName(url));

  if (changeStatus) {
    putJson(restUrl, '{ status: ' + changeStatus + '}', function(data) {
      jsonPage(restUrl, page, buildItemPage);
    });
  } else {
    jsonPage(restUrl, page, buildItemPage);
  }
  options.dataUrl = url.href;
  $.mobile.changePage(page, options);
}

// Sets up common elements of the item page. Calls itemDetails for item-specific
// elements.
function buildItemPage(page, item) {
  var h1 = pageHeader(page).find('h1');
  var status = item['status'] == 'new' ? 'archived' : 'new';
  var link;

  link  = '<a href="#item?id=' + item['id'] + '&changeStatus=' + status + '" class="ui-btn-right" data-theme="b">';
  link +=   item['status'] == 'new' ? 'Archive' : 'Un-archive';
  link += '</a>';

  h1.next('a').remove();
  h1.after(link);
  pageContent(page, itemDetails(item));

  // Not exactly sure why we need both of these, but they are required to get
  // the archive link to be styled properly.
  page.page();
  page.trigger('create');
}