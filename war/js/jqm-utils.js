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


// Set the content area of a page to the given HTML.
// Return the Content element in case the caller needs to do something with it.
//
// page: the page we're working with
// markup (optional): if not specified, just return the content element
function pageContent(page, markup) {
  var content = page.children(':jqmData(role=content)');

  if (markup) {
    content.html(markup);
  }
  return content;
}


// Returns the value of a named parameter from a given URL.
function getParameterByName(url, name) {
  var match = RegExp('[?&]' + name + '=([^&]*)').exec(url);
  return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
}

