
/* configure*/

const Host = '';;
const ServletName = '/FindAndEat/'

/**
 * @author Thomas Yeh
 */


/* DOM base function */

/**
 * create new dom element with elements
 * 
 * @param {String}
 *            tag
 * @param {Object}
 *            elements
 * @return {Object} HTML Object
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
 * 
 * @param {String}
 *            id
 * @return {Object} HTML Object
 */
function DomGetById(id) {
    return document.getElementById(id);
}

/**
 * mimic jquery $()
 * 
 * @param {String}
 *            tag
 * @param {Object}
 *            elements
 * @return {Object} HTML Object
 */
function $(tag, elements) {
    if (tag.charAt(0) === '<') {
        tag = tag.replace('<', '').replace('>', '');
        return DomCreateElement(tag, elements);
    } else {
        return DomGetById(tag);
    }
}



/* Ajax base function */


/**
 * AJAX pack wrap
 * 
 * @type {Object}
 */
var AJAX_PACK = {
    /**
	 * @param {String}
	 *            method GET PUT POST DELETE
	 * @param {String}
	 *            url [description]
	 * @param {RES_DEAL_FUN}
	 *            resDealFunction [description]
	 * @param {String}
	 *            data package data
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
 * 
 * @type {Object}
 */
var RES_DEAL_FUN = {
    /**
	 * create method base on the code
	 * 
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
 * 
 * @param {AJAX_PACK}
 *            pack
 */
function AJAXconnect(pack) {
    var xhr = new XMLHttpRequest();
    console.log(pack.url());
    xhr.open(pack.method, pack.url(), true);
    xhr.onload = function() {
        if (pack.res.f.hasOwnProperty(xhr.status)) {
            pack.res.f[xhr.status](xhr);
        } else {
        	//TODO
        }
    };

    if (pack.data != null) {
        xhr.send(pack.data);
    } else {
        xhr.send();
    }
}

//define global value 
var user_id = '';
var user_name='';
var term_now = 'dinner';

var lat = 37.354314;
var lon = -121.984203;

/*log in cennction*/
function login_init() {
    

    var ajaxDealFun = RES_DEAL_FUN.createNew();
    ajaxDealFun.createResponseFun(200,
        function(xhr) {
            var JsonData = JSON.parse(xhr.responseText);
            user_id = JsonData.user_id;
            console.log(JsonData.status);
            console.log(JsonData.user_id);
            console.log(JsonData.name);  
            ajax_connect_init();
        }
    );

    var url = function() {
        return Host + ServletName+'LoginServlet';
    }
    var loginPack = AJAX_PACK.createNew('GET', url, ajaxDealFun);
    AJAXconnect(loginPack);
}

login_init();


/**
 * initial the ajax connection 
 * 1.book mark connection 
 * 2.recommand connection
 * 3.search connection 
 */
function ajax_connect_init() {
    
	//define the processing function 200
	var ajaxDealFun = RES_DEAL_FUN.createNew();
    ajaxDealFun.createResponseFun(200,
        function(xhr) {
    		var JsonData = JSON.parse(xhr.responseText);     
            console.log(JsonData);
        }
    );

    console.log(user_id);
    //1.get the book mark list for the user
    var url = function() {
        return  Host +ServletName+'BookMarkServlet?user_id='+user_id;
    }
    var gBookMarkPack = AJAX_PACK.createNew('GET', url, ajaxDealFun);
    AJAXconnect(gBookMarkPack);
   
    
    //2.get the recommendataion list for the user
    url = function() {
        return Host + ServletName + 'RecommendationServlet?user_id=' + user_id;
    }
    var gRecommandPack = AJAX_PACK.createNew('GET', url, ajaxDealFun);
    AJAXconnect(gRecommandPack);
    
        
    //3.get the search list for the user
    url = function() {
        return Host+ ServletName+ 'SearchServlet?user_id='+ user_id +'&lat=' + lat + '&lon=' + lon + '&term=' + term_now;
    }
    var gSearchPack = AJAX_PACK.createNew('GET', url, ajaxDealFun); 
    AJAXconnect(gSearchPack);
    
}






