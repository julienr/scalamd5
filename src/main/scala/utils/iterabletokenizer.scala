package utils;
import java.io._;
import java.util;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.lang.Math;

/**
* Specialized tokenizer for doom3's files
* This basically is just a wrapper around java's StringTokenizer 
* that provides Iterator features
* 
*/
private class IterableTokenizer(fileName: String) extends Iterator[String] {
  var bufStream : BufferedInputStream = null
  
  var tokenizer : java.io.StreamTokenizer = null
  
  //initialize the stream
  reset
  
  /* reset the stream at its start position
  * FIXME: could perhaps be improved through the use of InputStream's mark() */
  def reset = {
    bufStream = new BufferedInputStream(new FileInputStream(fileName))
    //tokenizer = null
    tokenizer = new StreamTokenizer(bufStream)
    tokenizer.eolIsSignificant(false)
    tokenizer.slashStarComments(true)
    tokenizer.slashSlashComments(false)
    
    //workaround for a bug in StreamTokenizer which make impossible
    //to disable number parsing
    tokenizer.ordinaryChar('.')
    tokenizer.ordinaryChar('-')
    tokenizer.ordinaryChars('0','9')
    tokenizer.wordChars('.','.')
    tokenizer.wordChars('-','-')
    tokenizer.wordChars('0','9')
    tokenizer.ordinaryChar('_')
    tokenizer.wordChars('_','_')
  }

  def hasNext: Boolean = { 
    val ret = tokenizer.nextToken != StreamTokenizer.TT_EOF
    tokenizer.pushBack
    ret
  }
     
  /* iterate through the tokens until 'str' is found.
   * throws an exception if str can't be found
   */
  def accept(str: String) : Unit = {
    while (hasNext) {
      val t = next
      if (t == str) return
    }
    error(str + " not found")
  }
  def next() : String = { 
	  tokenizer.nextToken
    
    tokenizer.ttype match {
    case StreamTokenizer.TT_WORD =>
      tokenizer.sval
    case _ => //StreamTokenizer API is completly f*cked up and sometimes, when the token
              //is only one character, its value is stored in ttype...
    	new String()+tokenizer.ttype
    }
  }
}
