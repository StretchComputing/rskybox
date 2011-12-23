'use strict';

var RMODULE = (function (my, $) {

  var
    buildListPage,
    ITEM_PAGE;

  ITEM_PAGE = '#item';

  my.getItemPath = function () {
    return '/' + my.getItemName();
  };

  my.getNewItemName = function () {
    return 'new';
  };

  //
  // Generic List Functions
  //

  buildListPage = function (page, list) {
    var i, item, items, markup;

    markup = '<ul data-role="listview">';
    items = list[my.getItemName()];
    for (i = 0; i < items.length; i += 1) {
      item = items[i];
      markup += '<li><a href="' + ITEM_PAGE + '?item_id=' + item.id + '">' + my.getItemLinkText(item) + '</a></li>';
    }
    markup += '</ul>';
    my.pageContent(page, markup).find(':jqmData(role=listview)').listview();
    $('#new_item_button').attr('href', ITEM_PAGE + '?item_id=' + my.getNewItemName());
  };

  //
  // Generic Item Functions
  //

  my.init = function () {
    $('#index').live('pageshow', function () {
      my.jsonPopulate(my.getRestPrefix() + my.getItemPath(), $('#index'), buildListPage);
    });

    // set up the page(s) we need to build dynamically
    my.dynamicPages([{
      page: ITEM_PAGE,
      'function': my.itemPage
    }]);

    $(ITEM_PAGE).live('pagecreate', function () {
      // my.saveItem must be defined in the specific pages
      $(this).find('form').submit(my.saveItem);
    });

    $('#delete').live('pagecreate', function () {
      $('#delete_item').click(function (event) {
        var restUrl;

        restUrl = my.getRestPrefix() + my.getItemPath() + '/' + $('#item_id').val();
        my.deleteJson(restUrl, null, function () {
          event.preventDefault();
          $.mobile.changePage('#index');
        });
      });
    });
  };

  return my;

}(RMODULE || {}, jQuery));
