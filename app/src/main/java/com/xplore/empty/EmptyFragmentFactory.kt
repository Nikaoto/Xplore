package com.xplore.empty


import android.os.Bundle
import android.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xplore.R
import kotlinx.android.synthetic.main.empty_layout.*
import java.util.*

/**
 * Created by Nikaoto on 8/9/2017.
 *
 * ეს ფრაგმენტი ჩნდება როდესაც ლეიაუთში არაფერია
 *
 * This is shown when a layout has nothing to display
 */

class EmptyFragmentFactory(var type: Int = 6) {

    private val ascii = ArrayList<String>()

    init {
        initAsciiList()

        if (type > ascii.size - 1 || type < 0) {
            type = Random(System.currentTimeMillis()).nextInt(ascii.size)
        }
    }

    fun getSupportFragment(): SupportFragment {
        val fragment = SupportFragment()
        val args = Bundle()
        args.putString("asciiText", ascii[type])
        fragment.arguments = args
        return fragment
    }

    fun getFragment(): NormalFragment {
        val fragment = NormalFragment()
        val args = Bundle()
        args.putString("asciiText", ascii[type])
        fragment.arguments = args
        return fragment
    }

    class SupportFragment() : android.support.v4.app.Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, instState: Bundle?)
                = inflater.inflate(R.layout.empty_layout, container, false)

        override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            nothingHereAsciiTextView.text = arguments.getString("asciiText")
        }
    }

    class NormalFragment() : Fragment() {

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, instState: Bundle?)
                = inflater.inflate(R.layout.empty_layout, container, false)

        override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            nothingHereAsciiTextView.text = arguments.getString("asciiText")
        }
    }

    private fun initAsciiList() {
        ascii.add("""
                                            .
                                  .         ;
     .              .              ;%     ;;
       ,           ,                :;%  %;
        :         ;                   :;%;'     .,
,.        %;     %;            ;        %;'    ,;
 ;       ;%;  %%;        ,     %;    ;%;    ,%'
  %;       %;%;      ,  ;       %;  ;%;   ,%;'
   ;%;      %;        ;%;        % ;%;  ,%;'
    `%;.     ;%;     %;'         `;%%;.%;'
     `:;%.    ;%%. %@;        %; ;@%;%'
        `:%;.  :;bd%;          %;@%;'
          `@%:.  :;%.         ;@@%;'
            `@%.  `;@%.      ;@@%;
              `@%%. `@%%    ;@@%;
                ;@%. :@%%  %@@%;
                  %@bd%%%bd%%:;
                    #@%%%%%:;;
                    %@@%%%::;
                    %@@@%(o);  . '
                    %@@@o%;:(.,'
                `.. %@@@o%::;
                   `)@@@o%::;
                    %@@(o)::;
                   .%@@@@%::;
                   ;%@@@@%::;.
                  ;%@@@@%%:;;;.
              ...;%@@@@@%%:;;;;,..
""")

        ascii.add("""
 ^  ^  ^   ^      ___I_      ^  ^   ^  ^  ^   ^  ^
/|\/|\/|\ /|\    /\-_--\    /|\/|\ /|\/|\/|\ /|\/|\
/|\/|\/|\ /|\   /  \_-__\   /|\/|\ /|\/|\/|\ /|\/|\
/|\/|\/|\ /|\   |[]| [] |   /|\/|\ /|\/|\/|\ /|\/|\
""")
        ascii.add("""
                  /|_
                 /   |_
                /     /
               /      >
              (      >
             /      /
            /     /
           /      /
        __/      \_____
       /'             |
        /     /-\     /
       /      /  \--/
      /     /
     /      /
    (      >
   /      >
  /     _|
 /  __/
/_/
""")

        ascii.add("""
                        ,-.
                    _,-' - `--._
                  ,'.:  __' _..-)
                ,'     /,o)'  ,'
               ;.    ,'`-' _,)
             ,'   :.   _.-','
           ,' .  .    (   /
          ; .:'     .. `-/
        ,'       ;     ,'
     _,/ .   ,      .,' ,
   ,','     .  .  . .\,'..__
 ,','  .:.      ' ,\ `\)``
 `-\_..---``````-'-.`.:`._/
 ,'   '` .` ,`- -.  ) `--..`-..
 `-...__________..-'-.._  \
    ``--------..`-._ ```
                 ``
""")

        ascii.add("""
                      _       _._
               _,,-''' ''-,_ }'._''.,_.=._
            ,-'      _ _    '        (  @)'-,
          ,'  _..==;;::_::'-     __..----'''}
         :  .'::_;==''       ,'',: : : '' '}
        }  '::-'            /   },: : : :_,'
       :  :'     _..,,_    '., '._-,,,--\'    _
      :  ;   .-'       :      '-, ';,__\.\_.-'
     {   '  :    _,,,   :__,,--::',,}___}^}_.-'
     }        _,'__''',  ;_.-''_.-'
    :      ,':-''  ';, ;  ;_..-'
_.-' }    ,',' ,''',  : ^^
_.-''{    { ; ; ,', '  :
      }   } :  ;_,' ;  }
       {   ',',___,'   '
        ',           ,'
          '-,,__,,-'
""")

        ascii.add("""
o    o     __ __
 \  /    '       `
  |/   /     __    \
(`  \ '    '    \   '
  \  \|   |   @_/   |
   \   \   \       /--/
    ` ___ ___ ___ __ '
""")

        ascii.add("""
    .----.   @   @
   / .-"-.`.  \v/
   | | '\ \ \_/ )
 ,-\ `-.' /.'  /
'---`----'----'
""")

        ascii.add("""
,--.::::::::::::::::::::::::::::::::::::....:::::::
    )::::::::::::::::::::::::::::::::..      ..::::
  _'-. _:::::::::::::::::::::::::::..   ,--.   ..::
 (    ) ),--.::::::::::::::::::::::.   (    )   .::
             )-._::::::::::::::::::..   `--'   ..::
_________________):::::::::::::::::::..      ..::::
::::::::::::::::::::::::::::::::::::::::....:::::::
:::::::::::::::::::::::::::::::::::::::::::::::::::
:::::::::::::::::::::::::::::::::::::::::::::::::::
!:!:!:!:!:!:!:!:!:!:!:!:!:!:!:!:!:!:!:!:!:!:!:!:!:!
!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
|!|!|!|!|!|!|!|!|!|!|!|!|!|!|!|!|!|!|!|!|!|!|!|!|!|
|||||||||||||||||||||||||||||||||||||||||||||||||||
I|I|I|I|I|I|I|I|I|I|I|I|I|I|I|I|I|I|I|I|I|I|I|I|I|I
IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII
""")

    }
}