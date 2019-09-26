exports.generateCode = function(){
    var code="";
    for (i = 0; i < 7; i++){
        code = code + Math.floor((Math.random() * 10));
    }
    return code
}
