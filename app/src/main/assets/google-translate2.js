/*!***************************************************
 * google-translate.js v1.0.6
 * https://Get-Web.Site/
 * author: Vitalii P.
 *****************************************************/


/* Вы можете перенести данный конфиг в head своего сайта, чтобы динамически конфигурировать значения при помощи данных из CMS */
/* You can transfer this config to the head of your site to dynamically configure values using data from the CMS */


//document.addEventListener("DOMContentLoaded", (event) => {
//    var script2 = document.createElement("script");
//	script2.src = `https://cdn.jsdelivr.net/npm/js-cookie@2/src/js.cookie.min.js`;
//	document.getElementsByTagName("head")[0].appendChild(script2);
//	var script = document.createElement("script");
//	script.src = `//translate.google.com/translate_a/element.js?cb=TranslateWidgetIsLoaded`;
////	script.src = `https://image01.iturrit.com/script/translate-element.js?cb=TranslateWidgetIsLoaded`;
//	document.getElementsByTagName("head")[0].appendChild(script);
//});

function TranslateWidgetIsLoaded() {
	TranslateInit();
}

function loadJsPlugin() {
	var script2 = document.createElement("script");
	script2.src = `https://cdn.jsdelivr.net/npm/js-cookie@2/src/js.cookie.min.js`;
	document.getElementsByTagName("head")[0].appendChild(script2);
	var script = document.createElement("script");
	script.src = `//translate.google.com/translate_a/element.js?cb=TranslateWidgetIsLoaded`;
//	script.src = `https://image01.iturrit.com/script/translate-element.js?cb=TranslateWidgetIsLoaded`;
	document.getElementsByTagName("head")[0].appendChild(script);
}

//
//function TranslateInit() {
////	if (config.langFirstVisit && !Cookies.get("googtrans")) {
////		/* Если установлен язык перевода для первого посещения и куки не назначены */
////		/* If the translation language is installed for the first visit and cookies are not assigned */
////		TranslateCookieHandler("/auto/" + config.langFirstVisit);
////	}
//
//    var googleTranslateElement = document.createElement('div');
//    googleTranslateElement.style.visibility = "hidden";
//    var bodyFirstChild = document.body.firstElementChild;
//    document.body.insertBefore(googleTranslateElement, bodyFirstChild.nextElementSibling);
//
//
//    let code = TranslateGetCode();
//
//    TranslateHtmlHandler(code);
//    /* Инициализируем виджет с языком по умолчанию */
//    /* Initialize the widget with the default language */
//    new google.translate.TranslateElement({
////		pageLanguage: "ru",
//        multilanguagePage: true,
//        autoDisplay: false,// Your page contains content in more than one languages
//    },'google_translate_element');
//}

function TranslateInit() {
//	if (config.langFirstVisit && !Cookies.get("googtrans")) {
//		/* Если установлен язык перевода для первого посещения и куки не назначены */
//		/* If the translation language is installed for the first visit and cookies are not assigned */
//		TranslateCookieHandler("/auto/" + config.langFirstVisit);
//	}

    let code = TranslateGetCode();

    TranslateHtmlHandler(code);
    /* Инициализируем виджет с языком по умолчанию */
    /* Initialize the widget with the default language */
    new google.translate.TranslateElement({
//		pageLanguage: "ru",
        multilanguagePage: true,
        // Your page contains content in more than one languages
    });

    registerDomObserver('skiptranslate', function(element) {
        console.log('Found the skiptranslate element:', element);
        hideGoogleTranslateElment()
        // 在这里可以添加其他您想要的操作
    });
}

function hideGoogleTranslateElment() {
	var elements = document.getElementsByClassName('skiptranslate');
	for (var i = 0; i < elements.length; i++) {
        var element = elements[i];

        // 设置 z-index
        element.style.zIndex = '-9999';

        // 设置可见性
//        element.style.visibility = 'hidden';
        element.style.display = 'none';
        // 额外设置，以确保元素被"隐藏"
//        element.style.position = 'absolute';
//        element.style.opacity = '0';
//        element.style.pointerEvents = 'none';
//
//       //      如果元素是一个 iframe，我们可能需要额外的处理
        var iframe = element.querySelector('iframe');
        if (iframe) {
          iframe.style.display = 'none';
          iframe.style.height = '1px';
             iframe.style.overflow = 'hidden';
                  iframe.style.padding = '0';
                  iframe.style.margin = '0';
        }
      }
}

function TranslateGetCode() {
	/* Если куки нет, то передаем дефолтный язык */
	/* If there are no cookies, then we pass the default language */
	let lang =
		Cookies.get("googtrans") != undefined && Cookies.get("googtrans") != "null"
			? Cookies.get("googtrans")
			: "zh-CN";
	return lang.match(/(?!^\/)[^\/]*$/gm)[0];
}

function TranslateCookieHandler(val, domain) {
	/* Записываем куки /язык_который_переводим/язык_на_который_переводим */
	/* Writing down cookies /language_for_translation/the_language_we_are_translating_into */
	Cookies.set("googtrans", val, {
		domain: document.domain,
		path: '/'
	});
	Cookies.set("googtrans", val, {
		domain: "." + document.domain,
		path: '/'
	});

	if (domain == "undefined") return;
	/* записываем куки для домена, если он назначен в конфиге */
	/* Writing down cookies for the domain, if it is assigned in the config */
	Cookies.set("googtrans", val, {
		domain: domain,
		path: '/'
	});

	Cookies.set("googtrans", val, {
		domain: "." + domain,
		path: '/'
	});
}

function TranslateHtmlHandler(code) {
	/* Получаем язык на который переводим и производим необходимые манипуляции с DOM */
	/* We get the language to which we translate and produce the necessary manipulations with DOM */
	if (document.querySelector('[data-google-lang="' + code + '"]') !== null) {
		document
			.querySelector('[data-google-lang="' + code + '"]')
			.classList.add("language__img_active");
	}
}

function handleLanguageChange(langCode) {
    TranslateCookieHandler("/" + langCode, "");
    var iframe = document.getElementById(":0.container");
    if (iframe) {
        clickTranslateButton()
    } else {
        window.location.reload();
    }
}

function clickTranslateButton() {
    // 使用 CSS 转义语法来选择带有特殊字符的 ID
    clickButtonInIframe(":0.confirm")
}

function clickButtonInIframe(buttonId) {
    // 获取 iframe 元素
    var iframe = document.getElementById(":0.container");

    if (iframe) {
        // 确保 iframe 加载完成，访问其内部的 document
        var iframeDocument = iframe.contentDocument || iframe.contentWindow.document;

        // 从 iframe 内部的 document 中寻找指定 id 的按钮
        var button = iframeDocument.getElementById(buttonId);

        if (button) {
            // 如果找到了按钮，执行点击事件
            button.click();
            console.log('按钮 ' + buttonId + ' 已被点击');
        } else {
            console.log('未找到按钮: ' + buttonId);
        }
    } else {
        console.log('未找到 iframe: ' + iframeId);
    }
}

function triggerRestoreButton() {
    // 调用该函数并传入 iframe 和按钮的 ID
    clickButtonInIframe( ':0.restore');
}

function isElementHidden(elementId) {
    // 获取 iframe 元素
    var iframe = document.getElementById(":0.container");

    if (iframe) {
        // 确保 iframe 加载完成，访问其内部的 document
        var iframeDocument = iframe.contentDocument || iframe.contentWindow.document;

        // 从 iframe 内部的 document 中寻找指定 id 的按钮
        var element = iframeDocument.getElementById(elementId);

        // 检查元素是否存在
        if (element) {
            // 获取元素的计算样式
            const computedStyle = window.getComputedStyle(element);

            // 检查display属性是否为'none'
            return computedStyle.display === 'none';
        } else {
            console.log(`未找到ID为${elementId}的元素`);
            // 如果元素不存在，我们返回null或者抛出一个错误，取决于你的需求
            return null;
        }
    } else {
        console.log('未找到 iframe: ' + iframeId);
    }
}

function isCurrentPageTranslated() {
    return !isElementHidden(":0.finishSection")
}

function registerDomObserver(targetClassName, callback) {
    // 创建一个函数来检查新添加的节点
    function checkNewNode(addedNode) {
        if (addedNode.nodeType === 1 && addedNode.classList.contains(targetClassName)) {
            callback(addedNode);
        }
    }

    // 创建一个MutationObserver实例
    const observer = new MutationObserver(function(mutations) {
        mutations.forEach(function(mutation) {
            if (mutation.type === 'childList') {
                mutation.addedNodes.forEach(checkNewNode);
            }
        });
    });

    // 配置观察选项
    const config = { childList: true, subtree: true };

    // 开始观察整个 document body
    observer.observe(document.body, config);

    // 返回观察器实例，以便later可以停止观察
    return observer;
}

