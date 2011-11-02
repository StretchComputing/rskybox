var RMODULE = (function (my, $) {
  'use strict';

  var
    buildListPage,
    buildItemPage,
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

    restUrl = my.getRestPrefix() + '/' + my.itemName() + (status ? '?status=' + status : '');
    my.jsonPopulate(restUrl, page, buildListPage);
  };

  // Generic function to build the list of items.
  //
  // Calls listItem which is defined per type of item in its HTML file.
  // list: JSON object containing the list elements
  buildListPage = function (page, list) {
    var display, i, item, markup;

    markup = '<ul data-role="listview">';
    for (i = 0; i < list[my.itemName()].length; i += 1) {
      item = list[my.itemName()][i];
      display = item.date + ' - ' + item.userName + ' - ' + item.instanceUrl;
      markup += '<li><a href="#item?id=' + item.id + '">' + display + '</a></li>';
    }
    markup += '</ul>';
    my.pageContent(page, markup).find(':jqmData(role=listview)').listview();
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

    changeStatus = my.getParameterByName(url, 'changeStatus');
    restUrl = my.getRestPrefix() + '/' + my.itemName() + '/' + my.getParameterByName(url, 'id');
    if (changeStatus) {
      my.putJson(restUrl, '{ status: ' + changeStatus + ' }', function () {
        my.jsonPopulate(restUrl, page, buildItemPage);
      });
    } else {
      my.jsonPopulate(restUrl, page, buildItemPage);
    }
  };

  // Sets up common elements of the item page. Calls itemDetails for item-specific
  // elements.
  buildItemPage = function (page, item) {
    var h1, link, status;

    status = item.status === 'new' ? 'archived' : 'new';

    link  = '<a href="#item?id=' + item.id + '&changeStatus=' + status + '" class="ui-btn-right" data-theme="b">';
    link +=   item.status === 'new' ? 'Archive' : 'Un-archive';
    link += '</a>';

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

RMODULE.init();