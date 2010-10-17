/**
 * @author Dom Derrien
 */
(function() {
    var lg = location.search.indexOf('lg=fr') != -1 ? 'fr' : 'en',
        dm = 'http://anothersocialeconomy.appspot.com/widget/',
        id = "__ASE_wdgt",
        dc = document,
        buyItHandler = function(){
            var dv = dc.getElementById(id),
                bd = lg == 'fr' ? 'Réseau d\'achat locaux?' : 'Local Buying Network?',
                cl = lg == 'fr' ? 'Fermer [X]' : 'Close [X]',
                txt = window.getSelection ? window.getSelection() : dc.getSelection ? dc.getSelection() : dc.selection ? dc.selection.createRange().text : '',
                src = dm + 'ase.jsp?lg=' + lg + '&brand=' + bd + '&countryCode=CA&font-family=arial%2Chelvetica%2Csans-serif&font-family-brand=arial%2Chelvetica%2Csans-serif&font-family-title=arial%2Chelvetica%2Csans-serif&font-size=12px&font-size-brand=18px&font-size-title=14px&color=%23252525&color-odd-row=white&color-brand=%23252525&color-title=%23252525&color-link=005e9d&background-color=%23fff000&background-color-odd-row=%237f8082&criteria=' + escape(txt),
                ifr;
            if (dv) {
                ifr = dc.getElementById(id + '_ifr');
                ifr.src = src;
                dv.style.display = '';
            }
            else {
                var cpc = '100%',
                    hd;
                dv = dc.createElement('div');
                dv.id = id;
                dv.setAttribute('class', 'ase_buyItWidget');
                dc.getElementsByTagName('body')[0].appendChild(dv);
                hd = dc.createElement('span');
                hd.innerHTML = '<a href="#" onclick="document.getElementById(\''+id+'\').style.display=\'none\';return false;">'+ cl + '<a>';
                dv.appendChild (hd);
                ifr = dc.createElement('iframe');
                ifr.id = id + '_ifr';
                ifr.width = cpc;
                ifr.height = cpc;
                ifr.border = '0';
                ifr.src = src;
                dv.appendChild(ifr);
            }
        },
        installTab = function(){
            var hk = lg == 'fr' ? 'Achetez localement?' : 'Buy it locally?',
                ac = dc.createElement('a'),
                dv = dc.createElement('div'),
                lk = dc.createElement('link');
            lk.href = dm + 'widget-loader.css';
            lk.rel = 'stylesheet';
            lk.type = 'text/css';
            dc.getElementsByTagName('head')[0].appendChild(lk);
            ac.href = '#';
            ac.onclick = buyItHandler;
            ac.appendChild(dc.createTextNode(hk));
            dv.setAttribute("class", 'ase_buyItTab');
            dv.appendChild(ac);
            dc.getElementsByTagName('body')[0].appendChild(dv);
        };
    installTab();
})();
