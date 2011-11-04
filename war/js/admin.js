'use strict';

var RMODULE = (function (my, $) {

  var
    buildListPage,
    ITEM_PAGE,
    saveItem,
    validateUser;

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
      markup += '<li><a href="#item?id=' + item.id + '">' + my.getItemLinkText(item) + '</a></li>';
    }
    markup += '</ul>';
    my.pageContent(page, markup).find(':jqmData(role=listview)').listview();
  };

  //
  // Generic Item Functions
  //

  saveItem = function () {
    var restUrl, json;

    if (!validateUser()) {
      return false;
    }

    restUrl = my.getRestPrefix() + my.getItemPath;
    json = JSON.stringify({
      firstName: $('#firstName').val(),
      lastName: $('#lastName').val(),
      emailAddress: $('#emailAddress').val(),
      phoneNumber: $('#phoneNumber').val(),
      mobileCarrierId: $('#mobileCarrierId').val(),
      sendEmailNotifications: $('#sendEmailNotifications').prop('checked'),
      sendSmsNotifications: $('#sendSmsNotifications').prop('checked')
    });

    if ($('#id').val() !== my.getNewItemName()) {
      restUrl += '/' + $('#id').val();
    }
    my.putJson(restUrl, json, function () {
      history.back();
    });
    return false;
  };

  validateUser = function () {
    if (!$('#firstName').val() || !$('#lastName').val() || !my.validEmailAddress($('#emailAddress').val())) {
      window.alert('You must enter a first name, last name, and valid email address.');
      return false;
    }
    return my.validateSms();
  };

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
      $(this).find('form').submit(saveItem);

      $('#sendSmsNotifications').change(function () {
        my.enableSmsDetails($('#sendSmsNotifications').prop('checked'));
      });
    });

    $('#delete').live('pagecreate', function () {
      $('#delete_item').click(function (event) {
        var restUrl;

        restUrl = my.getRestPrefix() + my.getItemPath() + '/' + $('#id').val();
        my.deleteJson(restUrl, null, function () {
          event.preventDefault();
          $.mobile.changePage('#index');
        });
      });
    });
  };

  return my;

}(RMODULE || {}, jQuery));
