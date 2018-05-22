import java.io.*;
import java.util.*;

//SQRL parser
public class Parse
{
    private String line;
    private Scanner scan;
    private int[] vars;

    // constructor
    public Parse()
    {
        line = "";
        scan = new Scanner(System.in);
        vars = new int[26];
        for (int i = 0; i < 26; i++)
            vars[i] = 0;
    }

    // entry point into Parse
    public void run() throws IOException
    {
        String token;

        System.out.println("Welcome to SQRL...");

        token = getToken();
        parseCode(token);                  // ::= <code>
    }


    // parse token for <code>
    private void parseCode(String token) throws IOException
    {
        do {
            parseStmt(token, true);              // ::= <stmt <code> | <stmt>
            token = getToken();
        } while (!token.equals("."));
    }


    // parse token for <stmt>
    private void parseStmt(String token, boolean execute) throws IOException
    {
        int val;
        String str;
        boolean cond;

        if (token.equals("load"))
        {
            token = getToken();
            str = parseString(token);

            // interpreter excution part
            line = loadPgm(str) + line;
        }
        else if (token.equals("print"))
        {
            token = getToken();
            if (token.charAt(0) == '"')
            {
                str = parseString(token);

                // interpreter execution part
                if (execute)
                System.out.println(str);
            }
            else
            {
                val = parseExpr(token);

                // interpreter executin part
                if (execute)
                System.out.println(val);
            }
        }
        else if (token.equals("input"))
        {
            token = getToken();
            val = parseVar(token);

            // interpreter excutution part
            System.out.print("? ");
            val = scan.nextInt();
            storeVar(token, val);
        }
        else if (token.equals("if"))
        {
            token = getToken();
            cond = parseCond(token);
            token = getToken();
            parseStmt(token, cond && execute);

            // now see if we have an else
            token = getToken();
            if (token.equals("else"))
            {
                token = getToken();
                parseStmt(token, !cond && execute);
            }
            else
            line = token + line;
        }
        else if (isVar(token))
        {
        }
        else
            reportError(token);
    }


    // loads program from file
    private String loadPgm(String name) throws IOException
    {
        String buffer = "";
        File file = new File(name);
        Scanner fileScan = new Scanner(file);

        while (fileScan.hasNextLine())
            buffer += fileScan.nextLine() + "\n";

        return buffer;
    }


    // parses token for <expr>
    private int parseExpr(String token)
    {
        int val;
        String opToken;

        val = parseVal(token);
        opToken = getToken();

        switch (opToken.charAt(0))
        {
            case '+':
            token = getToken();        // ::= <val> + <val>
            val = val + parseVal(token);
            break;
            case '-':
            token = getToken();        // ::= <val> - <val>
            val = val - parseVal(token);
            break;
            case '*':
            token = getToken();        // ::= <val> * <val>
            val = val * parseVal(token);
            break;
            case '/':
            token = getToken();        // ::= <val> / <val>
            val = val / parseVal(token);
            break;
            default:
            line = opToken + line;
        }
        return val;
    }


    private boolean parseCond(String token)
    {
        int val;
        String opToken;

        val = parseVal(token);
        opToken = getToken();
        switch(opToken.charAt(0))
        {
            case '>':
            token = getToken();
            if (val > parseVal(token))
            {
                return true;
            }
            else
            {
                return false;
            }
            case '<':
            token = getToken();
            if (val < parseVal(token))
            {
                return true;
            }
            else
            {
                return false;
            }
            case '=':
            token = getToken();
            if (token.equals("="))
            {
                token=getToken();
                if (val == parseVal(token))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
            default:
            line = opToken + line;
        }
        return false;
    }


    // parse token for <val> and return its value
    private int parseVal(String token)
    {
        if (isNumeric(token))
            return Integer.parseInt(token);
        else
            return parseVar(token);

        //if (isVar(token))
        //    return parseVar(token);
        //
        // reportError(token);
        //
        //return -1;  // wont compile without this
    }


    // parse token for <var> and return its value
    private int parseVar(String token)
    {
        if (!isVar(token))
            reportError(token);

        return vars[(int)token.charAt(0) - 97];
    }

    // parse token to see if it's a variable
    private boolean isVar(String token)
    {
        return token.length() == 1 && isAlpha(token.charAt(0));
    }


    // stores var in vars array
    private void storeVar(String token, int val)
    {
        vars[(int)token.charAt(0) - 97] = val;
    }


    // return true if token is <num>
    private boolean isNumeric(String token)
    {
        for (int i=0; i<token.length(); i++)
            if (!isDigit(token.charAt(i)))
                return false;

        return true;
    }


    // checks if a number
    private boolean isDigit(char ch)
    {
        return ((int)ch) >= 48 && ((int) ch) <= 57;
    }


    // checks if alphabetic character
    private boolean isAlpha(char ch)
    {
        return ((int)ch) >= 97 && ((int)ch) <= 122;
    }


    // parse for <string>...this skips lexical analyzer getToken()
    private String parseString(String token)
    {
        int i;
        String str="";

        // grab string between quotes
        if(token.charAt(0) != '"')
            reportError(token);

        for (i=0; i<line.length(); i++)
            if (isNewLine(line.charAt(i)))
                reportError(token);
             else if (line.charAt(i) == '"')
                 break;

        if (i == line.length())
            reportError(token);

        str = line.substring(0,i);
        line = line.substring(i+1);

        return str;
    }


    // checks for new line
    private boolean isNewLine(char ch)
    {
        switch (ch)
        {
            case '\n':
            case '\r':
                return true;
            default:
                return false;
        }
    }


    // check for blank space
    private boolean isBlank(char ch)
    {
        switch (ch)
        {
            case ' ':
            case '\t':
                return true;
            default:
                return isNewLine(ch);
        }
    }


    // check for deliminter
    private boolean isDelim(char ch)
    {
        switch (ch)
        {
            case '.':
            case '"':
            case '+': case '-':
            case '*': case '/':
            case '>': case '<': case '=':
                return true;
            default:
                return isBlank(ch);
        }
    }


    // skip lead blanks
    private String skipLeadingBlanks(String buffer)
    {
        int i;
        for (i=0; i<buffer.length(); i++)
            if (!isBlank(buffer.charAt(i)))
                break;
        return buffer.substring(i);
    }


    // tokenizer
    private String getToken()
    {
        int i;
        String token;

        line = skipLeadingBlanks(line);

        while (line.length() == 0)
        {
            line = scan.nextLine();
            line = skipLeadingBlanks(line);
        }

        // grab out actual token
        for (i=0; i<line.length(); i++)
            if (isDelim(line.charAt(i)))
            {
                if (i==0)
                    i++;
                token = line.substring(0,i);
                line = line.substring(i);

                return token;
        
            }
            
        // entire line is token
        token = line;
        line = "";
        return token;
    }

    // reports a syntax error
    private void reportError(String token)
    {
        line += "\n";
        line = line.substring(0, line.indexOf("\n"));

        System.out.println("ERROR: " + token + line);
        for (int i=0; i < 7+token.length(); i++)
            System.out.print(" ");
        System.out.println("^");

        System.exit(-1);
    }
}