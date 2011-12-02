'use strict';

var RMODULE = (function (my) {

  my.addFormProperty = function (form, prop, val) {
    if (form && prop && val) {
      form[prop] = val;
    }
  };

  return my;
}(RMODULE || {}));
