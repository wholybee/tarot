package net.holybee.tarot

import android.os.Parcel
import android.os.Parcelable
import java.io.Serializable


enum class Card (val text: String, val value: Int, val roman: String, val filename: String ):Parcelable {
    FOOL("The Fool",0,"0","RWS_Tarot_00_Fool.jpg"),
    MAGICIAN("The Magician",1,"I","RWS_Tarot_01_Magician.jpg"),
    HIGH_PRIESTESS("The High Priestess",2,"ii","RWS_Tarot_02_High_Priestess.jpg"),
    EMPRESS("The Empress",3,"iii","RWS_Tarot_03_Empress.jpg"),
    EMPEROR("The Emperor",4,"iv","RWS_Tarot_04_Emperor.jpg"),
    HIEROPHANT("The Hierophant",5,"v","RWS_Tarot_05_Hierophant.jpg"),
    LOVERS("The Lovers",6,"vi","RWS_Tarot_06_TheLovers.jpg"),
    CHARIOT("The Chariot",7,"vii","RWS_Tarot_07_Chariot.jpg"),
    STRENGTH("Strength",8,"viii","RWS_Tarot_08_Strength.jpg"),
    HERMIT("The Hermit",9,"ix","RWS_Tarot_09_Hermit.jpg"),
    WHEELOFFORTUNE("Wheel of Fortune",10,"x","RWS_Tarot_10_Wheel_of_Fortune.jpg"),
    JUSTICE("Justice",11,"xi","RWS_Tarot_11_Justice.jpg"),
    HANGEDMAN("The Hanged Man",12,"xii","RWS_Tarot_12_Hanged_Man.jpg"),
    DEATH("Death",13,"xiii","RWS_Tarot_13_Death.jpg"),
    TEMPERANCE("Temperance",14,"xiv","RWS_Tarot_14_Temperance.jpg"),
    DEVIL("Devil",15,"xv","RWS_Tarot_15_Devil.jpg"),
    TOWER("The Tower",16,"xvi","RWS_Tarot_16_Tower.jpg"),
    STAR("The Star",17,"xvii","RWS_Tarot_17_Star.jpg"),
    MOON("The Moon",18,"xviii","RWS_Tarot_18_Moon.jpg"),
    SUN("The Sun",19,"xix","RWS_Tarot_19_Sun.jpg"),
    JUDGMENT("Judgment",20,"xx","RWS_Tarot_20_Judgement.jpg"),
    WORLD("The World",21,"xxi","RWS_Tarot_21_World.jpg"),
    ACECUPS("Ace of Cups",1,"i","Cups01.jpg"),
    TWOCUPS("Two of Cups",2,"ii","Cups02.jpg"),
    THREECUPS("Three of Cups",3,"iii","Cups03.jpg"),
    FOURCUPS("Four of Cups",4, "iv","Cups04.jpg"),
    FIVECUPS("Five of Cups",5,"v","Cups05.jpg"),
    SIXCUPS("Six of Cups",6,"vi","Cups06.jpg"),
    SEVENCUPS("Seven of Cups",7,"vii","Cups07.jpg"),
    EIGHTCUPS("Eight of Cups",8,"viii","Cups08.jpg"),
    NINECUPS("Nine of Cups",9,"ix","Cups09.jpg"),
    TENCUPS("Ten of Cups",10,"x","Cups10.jpg"),
    PAGECUPS("Page of Cups",11,"xi","Cups11.jpg"),
    KNIGHTCUPS("Knight of Cups",12,"xii","Cups12.jpg"),
    QUEENCUPS("Queen of Cups",13,"xiii","Cups13.jpg"),
    KINGCUPS("King of Cups",14,"xiv","Cups14.jpg"),
    ACEPENT("Ace of Pentacles",1,"i","Pents01.jpg"),
    TWOPENT("Two of Pentacles",2,"ii","Pents02.jpg"),
    THREEPENT("Three of Pentacles",3,"iii","Pents03.jpg"),
    FOURPENT("Four of Pentacles",4, "iv","Pents04.jpg"),
    FIVEPENT("Five of Pentacles",5,"v","Pents05.jpg"),
    SIXPENT("Six of Pentacles",6,"vi","Pents06.jpg"),
    SEVENPENT("Seven of Pentacles",7,"vii","Pents07.jpg"),
    EIGHTPENT("Eight of Pentacles",8,"viii","Pents08.jpg"),
    NINEPENT("Nine of Pentacles",9,"ix","Pents09.jpg"),
    TENPENT("Ten of Pentacles",10,"x","Pents10.jpg"),
    PAGEPENT("Page of Pentacles",11,"xi","Pents11.jpg"),
    KNIGHTPENT("Knight of Pentacles",12,"xii","Pents12.jpg"),
    QUEENPENT("Queen of Pentacles",13,"xiii","Pents13.jpg"),
    KINGPENT("King of Pentacles",14,"xiv","Pents14.jpg"),
    ACESWORDS("Ace of Swords",1,"i","Swords01.jpg"),
    TWOSWORDS("Two of Swords",2,"ii","Swords02.jpg"),
    THREESWORDS("Three of Swords",3,"iii","Swords03.jpg"),
    FOURSWORDS("Four of Swords",4, "iv","Swords04.jpg"),
    FIVESWORDS("Five of Swords",5,"v","Swords05.jpg"),
    SIXSWORDS("Six of Swords",6,"vi","Swords06.jpg"),
    SEVENSWORDS("Seven of Swords",7,"vii","Swords07.jpg"),
    EIGHTSWORDS("Eight of Swords",8,"viii","Swords08.jpg"),
    NINESWORDS("Nine of Swords",9,"ix","Swords09.jpg"),
    TENSWORDS("Ten of Swords",10,"x","Swords10.jpg"),
    PAGESWORDS("Page of Swords",11,"xi","Swords11.jpg"),
    KNIGHTSWORDS("Knight of Swords",12,"xii","Swords12.jpg"),
    QUEENSWORDS("Queen of Swords",13,"xiii","Swords13.jpg"),
    KINGSWORDS("King of Swords",14,"xiv","Swords14.jpg"),
    ACEWANDS("Ace of Wands",1,"i","Wands01.jpg"),
    TWOWANDS("Two of Wands",2,"ii","Wands02.jpg"),
    THREEWANDS("Three of Wands",3,"iii","Wands03.jpg"),
    FOURWANDS("Four of Wands",4, "iv","Wands04.jpg"),
    FIVEWANDS("Five of Wands",5,"v","Wands05.jpg"),
    SIXWANDS("Six of Wands",6,"vi","Wands06.jpg"),
    SEVENWANDS("Seven of Wands",7,"vii","Wands07.jpg"),
    EIGHTWANDS("Eight of Wands",8,"viii","Wands08.jpg"),
    NINEWANDS("Nine of Wands",9,"ix","Wands09.jpg"),
    TENWANDS("Ten of Wands",10,"x","Wands10.jpg"),
    PAGEWANDS("Page of Wands",11,"xi","Wands11.jpg"),
    KNIGHTWANDS("Knight of Wands",12,"xii","Wands12.jpg"),
    QUEENWANDS("Queen of Wands",13,"xiii","Wands13.jpg"),
    KINGWANDS("King of Wands",14,"xiv","Wands14.jpg");

    // Parcelable implementation
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Card> {
        override fun createFromParcel(parcel: Parcel): Card {
            return Card.valueOf(parcel.readString()!!)
        }

        override fun newArray(size: Int): Array<Card?> {
            return arrayOfNulls(size)
        }
    }

}

enum class GamePlay {
    NOTDEALT,
    DEALT,
    ASKED
}