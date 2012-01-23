var signups = {
  validEmail: {
    emailAddress: '1@test.com',
    testApp: true
  },
  validPhoneCredentials: {
    phoneNumber: '630-555-1212',
    mobileCarrierId: '1',
    testApp: true
  },
  missingCredentials: {
    testApp: true
  },
  missingEmailAndPhone: {
    mobileCarrierId: '1',
    testApp: true
  },
  invalidEmail: {
    emailAddress: '1test.com',
    testApp: true
  },
  missingCarrier: {
    phoneNumber: '630-555-1212',
    testApp: true
  },
  invalidPhoneNumber: {
    phoneNumber: '60-555-1212',
    mobileCarrierId: '1',
    testApp: true
  }
};


describe('Signup', function() {
  beforeEach(function() {
    this.signup = new rskybox.Signup();
  });

  describe('valid signup', function() {
    it('is created with valid email address', function() {
      this.signup.set(signups.validEmail);
      expect(this.signup.get('emailAddress')).toEqual('1@test.com');
    });
    it('is created with valid phone number credentials', function() {
      this.signup.set(signups.validPhoneCredentials);
      expect(this.signup.get('phoneNumber')).toEqual('630-555-1212');
      expect(this.signup.get('mobileCarrierId')).toEqual('1');
    });
  });

  describe('invalid signup', function() {
    beforeEach(function() {
      this.eventSpy = sinon.spy();
      this.signup.bind('error', this.eventSpy);
    });

    afterEach(function() {
      expect(this.eventSpy).toHaveBeenCalled();
      expect(this.signup.get('emailAddress')).toBeUndefined();
      expect(this.signup.get('phoneNumber')).toBeUndefined();
    });

    it('is not created with missing credentials', function() {
      this.signup.set(signups.missingCredentials);
    });
    it('is not created with missing email and phone', function() {
      this.signup.set(signups.missingEmailAndPhone);
    });
    it('is not created with invalid email', function() {
      this.signup.set(signups.invalidEmail);
    });
    it('is not created with missing carrier', function() {
      this.signup.set(signups.missingCarrier);
    });
    it('is not created with invalid phone number', function() {
      this.signup.set(signups.invalidPhoneNumber);
    });
  });

  describe('save signup', function() {
    beforeEach(function() {
      this.spy = sinon.spy(this.signup, 'handleSuccess');
      this.server = sinon.fakeServer.create();
    });
    afterEach(function() {
      expect(this.server.requests.length).toEqual(1);
      expect(this.spy).toHaveBeenCalled();
      this.server.restore();
    });

    describe('with valid credentials', function() {
      it('should be successful', function() {
        this.server.respondWith(
          'POST',
          this.signup.url,
          this.validResponse({
            apiStatus: '100',
            emailAddress: '1@test.com',
            confirmationCode: '123',
          }, 201)
        );
        this.signup.save(signups.validEmail, { success: this.signup.handleSuccess });
        this.server.respond();
      });
    });

    describe('already confirmed', function() {
      beforeEach(function() {
        this.spyWarning = sinon.spy(rskybox, 'displayWarning');
      });
      afterEach(function() {
        rskybox.displayWarning.restore();
      });

      it('fails when email already confirmed', function() {
        this.server.respondWith(
          'POST',
          this.signup.url,
          this.validResponse({
            apiStatus: '204',
            emailAddress: '1@test.com',
            confirmationCode: '123'
          }, 201)
        );
        this.signup.save(signups.validEmail, { success: this.signup.handleSuccess });
        this.server.respond();
        expect(this.spyWarning).toHaveBeenCalledWith(this.signup.warnings.api204);
      });

      it('fails when phone already confirmed', function() {
        this.server.respondWith(
          'POST',
          this.signup.url,
          this.validResponse({
            apiStatus: '205',
            phoneNumber: '630-555-1212',
            confirmationCode: '123'
          }, 201)
        );
        this.signup.save(signups.validPhoneCredentials, { success: this.signup.handleSuccess });
        this.server.respond();
        expect(this.spyWarning).toHaveBeenCalledWith(this.signup.warnings.api205);
      });
    });
  });
});
