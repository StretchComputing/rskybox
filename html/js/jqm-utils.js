// Dynamically inject pages
//
// Allows for bookmarking, page refreshing, and other proper URL handling for
// URLs that pass information via parameters.
//
// (JQM doc page located at <jqm site>/<version>/docs/pages/page-dynamic.html.)
//
// pages: a hash of page/function pairs to be watched for and responded to
//    page: the name of the page without any decoration (such as, '#')
//    function: function called to build the page
function dynamicPages(pages) {
  $(document).bind('pagebeforechange', function(event, data) {
    // Only handle pagebeforechange calls when loading a page via a URL.
    if (typeof data.toPage === "string") {
      // Only handle requests for the item page.
      var url = $.mobile.path.parseUrl(data.toPage);
      for (i = 0; i < pages.length; i++) {
        var re = new RegExp('^#' + pages[i]['page']);
        if (re.test(url.hash)) {
          // it's a little ugly, but this calls the function that was passed with
          // two parameters
          pages[i]['function'](url, data.options);
          event.preventDefault();
        }
      }
    }
  });
}

// Set up a page with JSON data.
//
// Makes a Ajax REST call to the given URL, then calls the given function to set
// up the page.
//
// restUrl: the URL for the REST call to get the JSON data
// page: the page to be built
// success: the function to call to actually build the page on success
function jsonPage(restUrl, page, success) {
  $.mobile.showPageLoadingMsg();
  $.getJSON(restUrl, function(data) {
    success(page, data);
    $.mobile.hidePageLoadingMsg();
  });
}


// Do an Ajax PUT call to update some data on the server.
//
// restUrl: URL for the REST call
// data: the data to be sent with the request
// callback: a function to call on success
function putJson(restUrl, data, success) {
  $.ajax({
    url: restUrl,
    type: 'PUT',
    contentType : 'application/json',
    data: data,
    success: success,
    dataType: 'json'
  });
}


// Get the name of the page from a JQM URL.
function getPageName(url) {
  return url.hash.replace(/\?.*$/, '');
}

// Set/Get the header area of a page.
//
// page: the page we're working with
// markup (optional): if not specified, just return the element
function pageContent(page, markup) {
  var content = page.children(':jqmData(role=content)');

  if (markup) {
    content.html(markup);
  }
  return content;
}


// Set/Get the content area of a page.
//
// page: the page we're working with
// markup (optional): if not specified, just return the element
function pageHeader(page, markup) {
  var header = page.children(':jqmData(role=header)');

  if (markup) {
    header.html(markup);
  }
  return header;
}


// Returns the value of a named parameter from a given JQM URL.
function getParameterByName(url, name) {
  var match = RegExp('[?&]' + name + '=([^&]*)').exec(url.hash);
  return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
}

