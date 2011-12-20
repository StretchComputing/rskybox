'use strict';

TestCase('FormFieldsTest', {
  setUp: function () {
    this.myform = {};
  },

  'test property added for non-blank form field': function () {
    RMODULE.addFormProperty(this.myform, 'newfield', 'some value');
    assertEquals(this.myform.newfield, 'some value');
  },

  'test property not added for blank form': function () {
    RMODULE.addFormProperty(this.myform, 'newfield', '');
    assertUndefined(this.myform.newfield);
  }
});
