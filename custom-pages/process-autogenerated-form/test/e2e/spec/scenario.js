'use strict';
describe('custom page', function() {



  beforeEach(function(){

    /* given a Process (id=2) and its Contract with following inputs:
    *
     {
       description:'Customer Account Name',
       name:'ticket_account',
       multiple:false,
       type:'BOOLEAN',
       inputs:[]
     },
     {
       description:'Description of your issue',
       name:'ticket_description',
       multiple:false,
       type:'INTEGER',
       inputs:[]
     },
     {
       description:null,
       name:'ticket_subject',
       multiple:true,
       type:'TEXT',
       inputs:[]
     },
     {
       description:null,
       name:'complex',
       multiple:true,
       inputs:[
         {
           description:null,
           name:'child1',
           multiple:false,
           type:'TEXT',
           inputs:[]
         },
         {
           description:null,
           name:'children',
           multiple:true,
           type:'DECIMAL',
           inputs:[]
         }
      ]
     },
     {
       description:'Business cost associated to this issue. In US Dollars.',
       name:'ticket_cost',
       multiple:false,
       type:'DECIMAL',
       inputs:[]
     },
     {
       description:'Ticket creation date',
       name:'ticket_date',
       multiple:false,
       type:'DATE',
       inputs:[]
     },
     {
       type: 'FILE',
       description: 'buisness archive',
       name: 'buisnessArchive',
       multiple: false,
       inputs: [
           {
           type: 'TEXT',
           description: 'Name of the file',
           name: 'filename',
           multiple: false,
           inputs: []
           },
           {
           type: 'BYTE_ARRAY',
           description: 'Content of the file',
           name: 'content',
           multiple: false,
           inputs: []
           }
       ]
     },
     {
       type: 'FILE',
       description: 'multiple file',
       name: 'screenShots',
       multiple: true,
       inputs: [
           {
           type: 'TEXT',
           description: 'Name of the file',
           name: 'filename',
           multiple: false,
           inputs: []
           },
           {
           type: 'BYTE_ARRAY',
           description: 'Content of the file',
           name: 'content',
           multiple: false,
           inputs: []
           }
       ]
     }
    */



    browser.get('http://localhost:3000/bonita/process-autogenerated-form/index.html?id=2');
  });

  afterEach(function(){
    browser.manage().logs().get('browser').then(function(browserLog) {
      console.log('log: ' + require('util').inspect(browserLog));
    });
  });

  it('should have a title', function() {
    expect(browser.getTitle()).toEqual('Register new Support Ticket');
  });

  it('should have a ticket_account boolean form input', function() {
    expect(element(by.id('ticket_account')).getAttribute('placeholder')).toEqual('Expecting a BOOLEAN value.');
  });

  it('should have a ticket_date date form input', function() {
	    expect(element(by.id('ticket_date')).getAttribute('placeholder')).toEqual('Expecting a DATE value. (2015-05-31)');
  });


});
