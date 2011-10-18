// Each type of item has its own index and archives page.
// The following blocks set up event handlers for these internal pages.
$('#index').live('pageshow', function() {
  setupListPage($('#index'));
});

$('#archives').live('pageshow', function() {
  setupListPage($('#archives'), 'archived');
});

// Retrieves and displays the appropriate list for the given page.
//
// page: the page we are setting up
// status (optional): default is 'new', but this parameter can override the default
function setupListPage(page, status) {
  var restUrl = '/rest/' + itemName() + (status ? '?status='+ status : '');

  $.mobile.showPageLoadingMsg();
  $.getJSON(restUrl, function(list) {
    pageContent(page, getMarkup(list)).find(':jqmData(role=listview)').listview();
    $.mobile.hidePageLoadingMsg();
  });
}

// Generic function to build the list of items.
//
// Calls listItem which is defined per type of item in its HTML file.
// list: JSON object containing the list elements
function getMarkup(list) {
  var markup = '<ul data-role="listview">';

  for (i = 0; i < list[itemName()].length; i++) {
    var item = list[itemName()][i];
    var display = item['date'] + ' - ' + item['userName'] + ' - ' + item['instanceUrl'];
    markup += '<li><a href="#item?id=' + item['id'] + '">' + display + '</a></li>';
  }
  markup += '</ul>'
  return markup;
}

dynamicPages([{
  'page' : 'item',
  'function' : showItem
}]);

// Generic function to show the item page.
//
// url: the url object of the current page
// options: jqm options for the current page (is this a correct statement?)
function showItem(url, options) {
  var changeStatus = getParameterByName(url.hash, 'changeStatus');
  var restUrl = '/rest/' + itemName() + '/' + getParameterByName(url.hash, 'id');
  $.mobile.showPageLoadingMsg();

  if (changeStatus) {
    $.ajax({
      url: restUrl,
      type: 'PUT',
      contentType : 'application/json',
      data: '{ status : ' + changeStatus + '}',
      success: function(item) {
        $.getJSON(restUrl, function(item) {
          setupItem(item, url, options);
        });
      },
      dataType: 'json'
    });
  } else {
    $.getJSON(restUrl, function(item) {
      setupItem(item, url, options);
    });
  }
}

// Sets up common elements of the item page. Calls itemDetails for item-specific
// elements.
function setupItem(item, url, options) {
  var id = url.hash.replace(/.*id=/, '');
  var pageSelector = url.hash.replace(/\?.*$/, '');
  var page = $(pageSelector);
  var header = page.children(':jqmData(role=header)');
  var content = page.children(':jqmData(role=content)');
  var h1 = header.find('h1');
  var status = item['status'] == 'new' ? 'archived' : 'new';
  var link = '<a href="#item?id=' + id + '&changeStatus=' + status + '" class="ui-btn-right" data-theme="b">';
  link += item['status'] == 'new' ? 'Archive' : 'Un-archive';
  link += '</a>';
  h1.next('a').remove();
  h1.after(link);
  content.html(itemDetails(item));
  // Not exactly sure why we need both of these, but they are required to get
  // the archive link to be styled properly.
  page.page();
  page.trigger('create');
  options.dataUrl = url.href;
  $.mobile.changePage(page, options);
  $.mobile.hidePageLoadingMsg();
}