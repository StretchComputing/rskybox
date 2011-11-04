'use strict';

var RMODULE = (function (my, $) {

  var
    buildListPage,
    itemPage,
    itemPage,
    listPage;

  //
  // Generic List Functions
  //

  // Retrieves and displays the appropriate list for the given page.
  //
  // page: the page we are setting up
  // status (optional): default is 'new', but this parameter can override the default
  listPage = function (page, status) {
    var restUrl;

    restUrl = my.getRestPrefix() + '/' + my.itemName();
    restUrl += (status ? '?status=' + status : '');
    my.jsonPopulate(restUrl, page, buildListPage);
  };

  // Generic function to build the list of items.
  //
  // page: the page we are working with
  // list: JSON object containing the list elements
  buildListPage = function (page, list) {
    var i, item, markup;

    markup = '<ul data-role="listview">';
    for (i = 0; i < list[my.itemName()].length; i += 1) {
      item = list[my.itemName()][i];
      markup += my.buildListItemContent(item);
    }
    markup += '</ul>';
    my.pageContent(page, markup).find(':jqmData(role=listview)').listview();
  };

  // Allow outsiders to provide their own list content
  my.buildListItemContent = function (item) {
    var display;

    display = item.date + ' - ' + item.userName + ' - ' + item.instanceUrl;
    return '<li><a href="#item?id=' + item.id + '">' + display + '</a></li>';
  };

  //
  // Generic Item Functions
  //

  // Generic function to show the item page.
  //
  // page: the jQuery element for the page
  // url: the URL object of the current page
  itemPage = function (page, url) {
    var changeStatus, restUrl;

    changeStatus = my.getParameterByName(url.hash, 'changeStatus');
    restUrl = my.getRestPrefix() + '/' + my.itemName() + '/' + my.getParameterByName(url.hash, 'id');
    if (changeStatus) {
      my.putJson(restUrl, '{ status: ' + changeStatus + ' }', function () {
        my.jsonPopulate(restUrl, page, my.buildItemPage);
      });
    } else {
      my.jsonPopulate(restUrl, page, my.buildItemPage);
    }
  };

  // Sets up common elements of the item page. Calls itemDetails for item-specific
  // elements.
  //
  // Public so it can be overridden.
  my.buildItemPage = function (page, item) {
    var h1, link, status;

    status = item.status === 'new' ? 'archived' : 'new';

    link  = '<a href="#item?id=' + item.id + '&changeStatus=' + status + '" class="ui-btn-right" data-theme="b">';
    link +=   item.status === 'new' ? 'Archive' : 'Un-archive';
    link += '<\/a>';

    h1 = my.pageHeader(page).find('h1');
    h1.next('a').remove();
    h1.after(link);
    my.pageContent(page, my.itemDetails(item));

    // Not exactly sure why we need both of these, but they are required to get
    // the archive link to be styled properly.
    page.page();
    page.trigger('create');
  };

  my.init = function () {
    // Each type of item has its own index and archives page.
    // The following blocks set up event handlers for these internal pages.
    $('#index').live('pageshow', function () {
      listPage($('#index'));
    });
    $('#archives').live('pageshow', function () {
      listPage($('#archives'), 'archived');
    });

    // The page(s) we need to handle and build dynamically. Let's us get information
    // from the URL and do special handling for these dynamically injected pages.
    my.dynamicPages([{
      page: '#item',
      'function': itemPage
    }]);
  };

  return my;
}(RMODULE || {}, jQuery));
