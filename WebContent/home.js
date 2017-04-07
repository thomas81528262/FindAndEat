

//avoid the namespace polution
(function () {
/*DOM base function*/

/**
 * create new dom element with elements
 * @param {String} tag      
 * @param {Object} elements 
 * @return {Object}          HTML Object
 */
function DomCreateElement(tag, elements) {
    var newElement = document.createElement(tag);
    for (var e in elements) {
        if (elements.hasOwnProperty(e)) {
            newElement[e] = elements[e];
        }
    }
    return newElement;
}

/**
 * get dom element by ID
 * @param {String} id 
 * @return {Object}          HTML Object
 */
function DomGetById(id) {
    return document.getElementById(id);
}

/**
 * mimic jquery $()
 * @param  {String} tag      
 * @param  {Object} elements 
 * @return {Object}          HTML Object
 */
function $(tag, elements) {
    if (tag.charAt(0) === '<') {
        tag = tag.replace('<', '').replace('>', '');
        return DomCreateElement(tag, elements);
    } else {
        return DomGetById(tag);
    }
}



/*Ajax base function*/


/**
 * AJAX pack wrap
 * @type {Object}
 */
var AJAX_PACK = {
    /**
     * @param  {String} method                  GET PUT POST DELETE
     * @param  {String} url                     [description]
     * @param  {RES_DEAL_FUN} resDealFunction   [description]
     * @param  {String} data                     package data
     * @return {Object}                 
     */
    createNew: function(method, url, resDealFunction, data) {
        var pack = {};
        pack.res = resDealFunction;
        pack.method = method;
        pack.url = url;
        pack.data = data;
        return pack;
    }
};


/**
 * AJAX response function
 * @type {Object}
 */
var RES_DEAL_FUN = {
    /**
     * create method base on the code
     * @return {Object} 
     */
    createNew: function() {
        var res = {};
        res.f = {};
        res.createResponseFun = function(code, f) {
            res.f[code] = f;
        };
        return res;
    }
};


/**
 * send XML http request base on the package information
 * @param  {AJAX_PACK} pack 
 */
function AJAXconnect(pack) {
    var xhr = new XMLHttpRequest();
    console.log(pack.url());
    xhr.open(pack.method, pack.url(), true);
    xhr.onload = function() {
    	
    	//find the dealing function by hash table
        if (pack.res.f.hasOwnProperty(xhr.status)) {
            pack.res.f[xhr.status](xhr);
        } else {
        	window.alert('worng user name or password or server is down');
            console.log('no method define');
        }
    };

    if (pack.data != null) {
        xhr.send(pack.data);
    } else {
        xhr.send();
    }
}


function login() {
    var username = $('username').value;
    var password = $('password').value;
    
    //protect the user password by md5
    password = md5(username + md5(password));

    var ajaxDealFun = RES_DEAL_FUN.createNew();
    
    //define the response function by return code
    ajaxDealFun.createResponseFun(200,
        function(xhr) {
            console.log('200');
            window.location.replace(xhr.responseURL);
        }
    );


    var url = function() {
        return './LoginServlet?user_id=' + username + '&password=' + password;
    }
    var loginPack = AJAX_PACK.createNew('POST', url, ajaxDealFun);
    AJAXconnect(loginPack);
}


function loginInit() {
    var recommandBtn = DomGetById('login-btn');
    recommandBtn.onclick = function() { login() };
}

loginInit();

})();
