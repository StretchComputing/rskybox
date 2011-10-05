// Utility Functions
// -----------------

// Get a query paramter from a url by its name.
function getParameterByName(url, name) {
  var match = RegExp('[?&]' + name + '=([^&]*)').exec(url);
  return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
}


// Livefeed Code
// -----------------

// Show the list of items.
//
// Listen for when the index/list page is shown to display a new list.
$('#index').live('pageshow', function() {
  getList($('#index'));
});

$('#archives').live('pageshow', function() {
  getList($('#archives'), 'archived');
});

function getList(page, status) {
  $.mobile.showPageLoadingMsg();
  var restUrl = '/rest/' + itemName() + (status ? '?status='+ status : '');
  $.getJSON(restUrl, function(list) {
    showList(page, list);
    $.mobile.hidePageLoadingMsg();
  });
}

// Generic function to show the list of items.
// Calls listItem which needs to be defined per type of item.
function showList(page, list) {
  var header = page.children(':jqmData(role=header)');
  var content = page.children(':jqmData(role=content)');
  var markup = '<ul data-role="listview">';
  for (i = 0; i < list[itemName()].length; i++) {
    var item = list[itemName()][i];
    var display = item['date'] + ' - ' + item['userName'] + ' - ' + item['instanceUrl'];
    markup += '<li><a href="#item?id=' + item['id'] + '">' + display + '</a></li>';
  }
  content.html(markup);
  content.find(':jqmData(role=listview)').listview();
}

// Dynamically inject item pages
//
// JQM doc page located at <jqm site>/<version>/docs/pages/page-dynamic.html.
$(document).bind('pagebeforechange', function(event, data) {
  // Only handle changepage calls whens loading a page URL.
  if (typeof data.toPage === "string") {
    // Only hanlde requests for the item page.
    var url = $.mobile.path.parseUrl(data.toPage);
    if (url.hash.search(/^#item/) != -1) {
      var change_status = getParameterByName(url.hash, 'change_status');
      if (change_status) {
        url.hash = url.hash.replace(/&.*/, '');
      }
      showItem(url, data.options, change_status);
      event.preventDefault();
    }
  }
});

// Generic function to show the item page.
//
// url: the url object of the current page
// options: jqm options for the current page (is this a correct statement?)
// change_status: whether the status of the item needs to be changed
function showItem(url, options, change_status) {
  $.mobile.showPageLoadingMsg();
  var restUrl = '/rest/' + itemName() + '/' + url.hash.replace(/.*id=/, '');

  if (change_status) {
    $.ajax({
      url: restUrl,
      type: 'PUT',
      contentType : 'application/json',
      data: '{ status : ' + change_status + '}',
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

// Sets up commone elments of the item page. Calls itemDetails for item-specific
// elements.
function setupItem(item, url, options) {
  var id = url.hash.replace(/.*id=/, '');
  var pageSelector = url.hash.replace(/\?.*$/, '');
  var page = $(pageSelector);
  var header = page.children(':jqmData(role=header)');
  var content = page.children(':jqmData(role=content)');
  var h1 = header.find('h1');
  var status = item['status'] == 'new' ? 'archived' : 'new';
  var link = '<a href="#item?id=' + id + '&change_status=' + status + '" class="ui-btn-right" data-theme="b">';
  link += item['status'] == 'new' ? 'Archive' : 'Un-archive';
  link += '</a>';
  h1.next('a').remove();
  h1.after(link);
  content.html(itemDetails(item));
  // Not exactly sure why we need both of these, but they are required to get
  // the archive link to be styled appropriately.
  page.page();
  page.trigger('create');
  options.dataUrl = url.href;
  $.mobile.changePage(page, options);
  $.mobile.hidePageLoadingMsg();
}