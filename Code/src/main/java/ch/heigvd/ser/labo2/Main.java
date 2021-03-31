/**
 * Noms des étudiants : Blanc Jean-Luc & Herzig Melvyn
 */

package ch.heigvd.ser.labo2;

import ch.heigvd.ser.labo2.coups.*;
import org.jdom2.Document;
import org.jdom2.Element;

import java.io.*;
import java.util.List;

import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;


class Main
{

   public static void main(String... args) throws Exception
   {
      Document document = readDocument(new File("tournois_fse.xml"));

      Element root = document.getRootElement();
      List<Element> tournois = root.getChild("tournois").getChildren();

      writePGNfiles(tournois);
   }

   /**
    * Cette méthode doit parser avec SAX un fichier XML (file) et doit le transformer en Document JDOM2
    *
    * @param file
    */
   private static Document readDocument(File file) throws JDOMException, IOException
   {
      return new SAXBuilder().build(file);
   }


   /**
    * Cette méthode doit générer un fichier PGN pour chaque partie de chaque tournoi recu en paramètre comme indiqué dans la donnée
    * <p>
    * Le nom d'un fichier PGN doit contenir le nom du tournoi ainsi que le numéro de la partie concernée
    * <p>
    * Nous vous conseillons d'utiliser la classe PrinterWriter pour écrire dans les fichiers PGN
    * <p>
    * Vous devez utiliser les classes qui sont dans le package coups pour générer les notations PGN des coups d'une partie
    *
    * @param tournois Liste des tournois pour lesquelles écrire les fichiers PGN
    *                 <p>
    *                 (!!! Un fichier par partie, donc cette méthode doit écrire plusieurs fichiers PGN !!!)
    */
   private static void writePGNfiles(List<Element> tournois)
   {
      // Pour chaque tournoi
      for (Element tournoi : tournois)
      {
         int gameNumber = 1;

         // Pour chaque partie du tournoi.
         for (Element partie : tournoi.getChild("parties").getChildren())
         {
            String resultFilename = tournoi.getAttributeValue("nom") + "_" + (gameNumber++) + ".PGN";

            // Création du fichier de sortie.
            try (PrintWriter out = new PrintWriter(new FileWriter(resultFilename)))
            {
               int turn = 1;
               boolean blackTurn = false;

               // Pour chaque coup
               for (Element coup : partie.getChild("coups").getChildren())
               {
                  // Création du coup spécial
                  CoupSpecial cs = getCoupSpecialFromString(coup.getAttributeValue("coup_special"));

                  // Est-ce un déplacement ou un roque ?
                  Element typeCoup = coup.getChild("deplacement");
                  Coup objCoup = null;
                  if (typeCoup != null) // Le coup est un déplacement
                  {
                     objCoup = getDeplacementFromElement(typeCoup, cs);
                  }
                  else // C'est donc un roque
                  {
                     typeCoup = coup.getChild("roque");
                     objCoup = getRoqueFromElement(typeCoup, cs);
                  }


                  String notationPGN = objCoup != null ? objCoup.notationPGN() : "";


                  if (blackTurn) // Coup des noirs
                  {
                     out.println(" " + notationPGN);

                     blackTurn = false;
                     ++turn;
                  }
                  else // Coup des blancs
                  {
                     out.print(turn + " " + notationPGN);

                     blackTurn = true;
                  }
               }
            }
            catch (IOException e)
            {
               System.out.println(e.getMessage());
               return;
            }
         }
      }
   }

   /**
    * Crée un coup spécial en fonction de readValue.
    * @param readValue Chaîne de caractère à convertir (sera convertie en majuscule).
    * @return Retourne le CoupSpecial correspondant a readValue sinon null.
    * @throws IllegalArgumentException si readValue ne correspond à aucune
    *        valeur de CoupSpecial.
    */
   private static CoupSpecial getCoupSpecialFromString(String readValue)
   {
      if (readValue == null) return null;

      return CoupSpecial.valueOf(readValue.toUpperCase());
   }

   /**
    * Crée une pièce en fonction de readValue.
    * @param readValue Chaîne de caractère à convertir (sera convertie en majuscule).
    * @return Retourne la pièce correspondant a readValue sinon null.
    * @throws IllegalArgumentException si readValue ne correspond à aucune
    *        valeur de CoupSpecial.
    */
   private static TypePiece getTypePieceFromString(String readValue)
   {
      if (readValue == null) return null;

      return TypePiece.valueOf(readValue);
   }

   /**
    * Retourne le type de roque de readValue
    * @param readValue readValue Chaîne de caractère à convertir.
    * @return Retourne un petit roque si la chaine commence par "petit" ou
    *         un grand groque si elle commance par "grand" (non sensible à la casse).
    * @throws IllegalArgumentException si readValue ne correspond à aucune
    *         valeur de CoupSpecial.
    */
   private static TypeRoque getTypeRoqueFromString(String readValue)
   {
      if (readValue == null) return null;

      return TypeRoque.valueOf(readValue.substring(0,5).toUpperCase());
   }

   /**
    * Transforme la lettre (premier caractère) en colonne et la seconde lettre en no de ligne.
    * @param readValue Ch Chaîne à convertir.
    * @return Retourne la case correspondante.
    * @throws StringIndexOutOfBoundsException si le string ne fait pas au moins 2 caractères
    * @throws NumberFormatException Si le second caractère n'est pas un entier.
    *
    */
   private static Case getCaseFromString(String readValue)
   {
      if (readValue == null) return null;

      return new Case(readValue.charAt(0), Integer.parseInt(readValue.substring(1,2)));
   }

   /**
    * Retourne le déplacement correspondant à eleDeplacement (déplacement parsé) et isSpecial.
    * @param elemDeplacement Element (deplacement) parsé.
    * @param isSpecial Type de coup spécial associé au déplacement.
    * @return Retourne l'objet deplacement sinon null en cas d'échec de construction.
    */
   private static Deplacement getDeplacementFromElement(Element elemDeplacement, CoupSpecial isSpecial)
   {
      try
      {
         TypePiece movedPiece = getTypePieceFromString(elemDeplacement.getAttributeValue("piece"));
         TypePiece killedPiece = getTypePieceFromString(elemDeplacement.getAttributeValue("elimination"));
         TypePiece promotedTo = getTypePieceFromString(elemDeplacement.getAttributeValue("promotion"));
         Case depart = getCaseFromString(elemDeplacement.getAttributeValue("case_depart"));
         Case arrivee = getCaseFromString(elemDeplacement.getAttributeValue("case_arrivee"));

         return new Deplacement(movedPiece, killedPiece, promotedTo, isSpecial, depart, arrivee);
      }
      catch(Exception e)
      {
         System.out.println("Oops: " + e.getMessage());
         return null;
      }
   }

   /**
    * Crée l'objet roque correspondant à l'élément parsé et au coup spécial.
    * @param elemRoque Element (roque) parsé.
    * @param isSpecial Le type spécial du coup associé.
    * @return Retourne l'objet roque associé.
    */
   private static Roque getRoqueFromElement(Element elemRoque, CoupSpecial isSpecial)
   {
      try
      {
         TypeRoque tr = getTypeRoqueFromString(elemRoque.getAttributeValue("type"));
         return new Roque(isSpecial, tr);
      }
      catch(Exception e)
      {
         System.out.println("Oops: " + e.getMessage());
         return null;
      }
   }
}