
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
            user_id=JsonData.user_id;
            $('user-name').innerText = 'Hi, ' + JsonData.name;
            ajax_connect_init();
        }
    );

    var url = function() {
        return Host + ServletName+'LoginServlet';
    }
    var loginPack = AJAX_PACK.createNew('GET', url, ajaxDealFun);
    AJAXconnect(loginPack);
}

/**
 * the global request function
 */
var searchRequest;
var bookMarkRequest;
var isBookMarkPage = false;


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
    		 listRestaurantData(JsonData);
        }
    );

    console.log(user_id);
    //1.get the book mark list for the user
    var url = function() {
        return  Host +ServletName+'BookMarkServlet?user_id='+user_id;
    }
    var gBookMarkPack = AJAX_PACK.createNew('GET', url, ajaxDealFun);
    var btn = $('bookmark-btn');
    btn.onclick = function() { AJAXconnect(gBookMarkPack);
    isBookMarkPage = true;};
   
    
    //2.get the recommendataion list for the user
    url = function() {
        return Host + ServletName + 'RecommendationServlet?user_id=' + user_id;
    }
    var gRecommandPack = AJAX_PACK.createNew('GET', url, ajaxDealFun);
    btn = $('recommand-btn');
    btn.onclick = function() { AJAXconnect(gRecommandPack); 
    isBookMarkPage = false; };
    
        
    //3.get the search list for the user
    url = function() {
        return Host+ ServletName+ 'SearchServlet?user_id='+ user_id +'&lat=' + lat + '&lon=' + lon + '&term=' + term_now;
    }
    var gSearchPack = AJAX_PACK.createNew('GET', url, ajaxDealFun); 
    btn = $('near-by-btn');
    btn.onclick = function() { AJAXconnect(gSearchPack); 
    isBookMarkPage = false;};
    
    searchRequest = function() {
        AJAXconnect(gSearchPack);
    }
    
    bookMarkRequest = function(business_id) {
        // Check whether this restaurant has been visited or not
        var li = $('restaurant-' + business_id);
        var bookIcon = $('bookMark-icon-' + business_id);
        var isBooked = li.dataset.is_booked !== 'true';

        // The request parameters
        var url = function() {
            return ServletName+ 'BookMarkServlet?user_id=' + user_id;
        };
        var req = JSON.stringify({
            user_id: user_id,
            is_booked: isBooked,
            business_id: business_id
        });
        

        var rFun = RES_DEAL_FUN.createNew()
        rFun.createResponseFun(200,
            function(xhr) {
                var data = JSON.parse(xhr.responseText);
                if (data.status === 'OK') {
                    li.dataset.is_booked = isBooked;
                    bookIcon.className = isBooked ? 'fa fa-bookmark' : 'fa fa-bookmark-o';
                    if (isBookMarkPage && !isBooked) {
                        li.nextElementSibling.remove();
                        li.remove();
                        
                    }

                }
            }
        );

        var ajaxPack = AJAX_PACK.createNew('POST', url, rFun, req);
        AJAXconnect(ajaxPack);
    }
    
}


/**
 * initial the drop function button
 */
function drop_btn_init() {

    var FUNC_BUT = {
        createNew: function(span, btn) {
            return function() {
                span.innerHTML = btn.innerHTML;
                term_now = btn.dataset.kind;
                 searchRequest();
            }
        }
    }

    const search_term = ['brunch', 'lunch', 'dinner'];
    var dropBtn = $('drop-down-botton');
    dropBtn.classList.remove('active');
    dropBtn.onclick = function() { dropBtn.classList.toggle('active'); }
    var btn = dropBtn.getElementsByTagName('li');
    var dropBtnSpan = dropBtn.getElementsByTagName('span')[0];

    for (var i = 0; i < btn.length; i++) {
        btn[i].onclick = FUNC_BUT.createNew(dropBtnSpan, btn[i]);
        btn[i].dataset.kind = search_term[i];
    }
}



//replace the  restaurant-list part
function listRestaurantData(jsonData) {
    var listSection = $('restaurant-list');
    listSection.innerHTML = '<hr>';

    for (var i = 0; i < jsonData.length; i++) {
        addRestaurantElement(listSection, jsonData[i]);
    }
}






function addRestaurantElement(listSection, jsonData) {
    var newRestaurant = createListRestaurant(jsonData);
    var image = createListImg(jsonData);
    var section = createListSection(jsonData);
    var address = createListAddress(jsonData);
    var bookMark = createListBookMark(jsonData);

    // the order can not be changed!!!!

    newRestaurant.dataset.is_booked = jsonData.is_booked;
    newRestaurant.appendChild(image);
    newRestaurant.appendChild(section);
    newRestaurant.appendChild(address);
    newRestaurant.appendChild(bookMark);
    listSection.appendChild(newRestaurant);
    listSection.appendChild($('<hr>'));
}


function createListBookMark(jsonData) {
    var bookMark = $('<p>', { className: 'bookMark-link' });
    bookMark.appendChild($('<i>', {
        id: 'bookMark-icon-' + jsonData.business_id,
        className: jsonData.is_booked ? 'fa fa-bookmark' : 'fa fa-bookmark-o'
    }));

    bookMark.onclick = function() {
        bookMarkRequest(jsonData.business_id);
    };
  
    return bookMark;
}


function createListSection(jsonData) {
    var section = $('<div>');

    // category
    var category = $('<p>', { className: 'restaurant-category' });
    category.innerHTML = jsonData.categories.join(', ');
    section.appendChild(category);


    // stars
    var stars = $('<div>', { className: 'stars' });
    // console.log(jsonData.stars);
    for (var i = 0; i < Math.floor(jsonData.stars); i++) {
        var star = $('<i>', { className: 'fa fa-star' });
        stars.appendChild(star);
    }

    if (('' + jsonData.stars).match(/\.5$/)) {
        stars.appendChild($('<i>', { className: 'fa fa-star-half-o' }));
    }

    section.appendChild(stars);
    return section;
}

function createListImg(jsonData) {
    return $('<img>', { src: jsonData.image_url });
}

function createListAddress(jsonData) {
    var address = $('<p>', { className: 'restaurant-address' });
    address.innerHTML = jsonData.full_address.replace(/,/g, '<br/>');
    return address;
}

function createListRestaurant(jsonData) {
    return $('<li>', {
        id: 'restaurant-' + jsonData.business_id,
        className: 'restaurant'
    });
}


login_init();
drop_btn_init();



