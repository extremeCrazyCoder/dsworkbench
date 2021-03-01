/*

The MIT License (MIT)

Copyright (c) 2021 Torridity

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”),
to deal in the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

The Software is provided “as is”, without warranty of any kind, express or implied, including but not limited to the warranties of merchantability,
fitness for a particular purpose and noninfringement. In no event shall the authors or copyright holders be liable for any claim,
damages or other liability, whether in an action of contract, tort or otherwise, arising from,
out of or in connection with the software or the use or other dealings in the Software.

*/
// ==UserScript==
// @name           DS Workbench Scripts
// @namespace      none
// @include        https://de*.die-staemme.de/game.php?*screen=place*
// @include        https://de*.die-staemme.de/game.php?screen=place*
// @include        https://de*.die-staemme.de/game.php?*screen=place
// @include        https://de*.die-staemme.de/game.php?*screen=market&mode=send*
// @include        https://de*.die-staemme.de/game.php?*mode=groups*
// @exclude        https://de*.die-staemme.de/game.php?*screen=place&mode=units
// @exclude        https://de*.die-staemme.de/game.php?*screen=place&mode=sim
// @exclude        https://de*.die-staemme.de/game.php?*screen=place&mode=neighbor
// @exclude        https://de*.die-staemme.de/game.php?*screen=place&try=confirm
// ==/UserScript==

/**Changelog
1.2
---

Fixed selectVillage() to allow village IDs with less than 3 digits

1.1 
---
Fixed an issue in selectVillages() which caused an error if a village was not visible on the current page (e.g. for users with more than 1000 villages)
Added focussing of attack button (open place, insert troops, focus 'Attack') and transport commit button (open market, insert resources, focus 'OK')

1.0 
---
Initial version
*/

var api = typeof unsafeWindow != 'undefined' ? unsafeWindow.ScriptAPI : window.ScriptAPI;
api.register( '48-Workbench Userscript', true, 'Torridity', 'support-nur-im-forum@die-staemme.de');

var $x = function(p, context) {
	if(!context){
		context = document;
	}
	var i, arr = [], xpr = document.evaluate(p, context, null, XPathResult.UNORDERED_NODE_SNAPSHOT_TYPE, null);
	for (i = 0; item = xpr.snapshotItem(i); i++)
		arr.push(item);
	return arr;
};

/**Select villages that have been inserted to the custom input field */
function selectVillages(){
	try{
		var ids = document.getElementById('village_ids').value.split(';');
		var valueSet = 0;
		for (var i = 0; i < ids.length; i++){
			var id = ids[i];
			if(id != null){
				//Version 1.1: Additional check to ensure that village is on current page
				var possibleInput = $x('//input[@value='+ id + ']')[0];
				if (possibleInput){
					possibleInput.checked = true;
					valueSet++;
				}
			}
		}
		if(valueSet > 0){
			//at least one village was found, set input fields background to green
			document.getElementById('village_ids').setAttribute('style', 'background-color:#00FF00');
		}else{
			//no village was found, mark input field red
			document.getElementById('village_ids').setAttribute('style', 'background-color:#FF0000');
		}
	}catch(err){
		//some error has occured, mark field red
		document.getElementById('village_ids').setAttribute('style', 'background-color:#FF0000');
	}
}

/**Parse arguments from village ID input field, appended after group_assign_table, 
or from location URL for troop/resource transfers */
function getArgs() { 
	var table = document.getElementById('group_assign_table');
	if(table != null){
		//we are in the groups view, add groups input field
		var input = document.createElement('input');
		input.setAttribute('class', 'vis');
		input.setAttribute('id', 'village_ids');
		input.setAttribute('size', '45');
		input.setAttribute('value', '<Bitte Dorf-IDs einfuegen>');
		input.addEventListener('keyup', function(){
			selectVillages();
		}, false);
		table.appendChild(input);
		return;
 	}

  //default handling of browser URL
   args = new Object();
   var query = location.search.substring(1); 

   var pairs = query.split("&"); 
   for(var i = 0; i < pairs.length; i++) { 
      var pos = pairs[i].indexOf('='); 
      if (pos == -1) continue; 
         var argname = pairs[i].substring(0,pos); 
         var value = pairs[i].substring(pos+1); 
         args[argname] = unescape(value); 
      } 

	if (args.type){
		//get type field to decide which entries are expected (troops or resources)
		type = parseInt(args.type);
	}else{
		//no valid type found
	 	type = -1;
	}

	if(type == 0){//add troops
		doInsertAction(new Array("spear", "sword", "axe", "archer", "spy", "light", "marcher", "heavy", "ram", "catapult", "knight", "snob"));
		//Version 1.1: Focus 'Attack' button if troops were added
		var attackButton = document.getElementsByName("attack")[0];
		if(attackButton){
			attackButton.focus();
		}		
	}else if(type == 1){//add resources
		doInsertAction(new Array("wood", "stone", "iron"));
		//Version 1.1: Focus 'OK' button if resources were added
		var possibleOK = $x('//input[@type="submit"]')[0];
		if(possibleOK){
			possibleOK.focus();
		}
	}
} 

/**Insert a list of elements by their names. Values are obtained from args array.*/
function doInsertAction(expectedElements){
	//go through all expected elements
   for (var i = 0; i < expectedElements.length; ++i){
	    //get form field for expected element	    
	    field = document.getElementsByName(expectedElements[i])[0];
   	    //if field was found and arguments contains a value for the expected element...
	    if(field != null && args[expectedElements[i]] != null){
			//...insert value
			field.value=args[expectedElements[i]];
      }
   }
}    

//fill args array
getArgs();
