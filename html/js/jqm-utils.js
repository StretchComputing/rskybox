// Returns the value of a named paramater from a given URL.
function getParameterByName(url, name) {
  var match = RegExp('[?&]' + name + '=([^&]*)').exec(url);
  return match && decodeURIComponent(match[1].replace(/\+/g, ' '));
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