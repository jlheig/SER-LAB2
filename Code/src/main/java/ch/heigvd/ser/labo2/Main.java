/**
 * Noms des étudiants : Blanc Jean-Luc & Herzig Melvyn
 */

package ch.heigvd.ser.labo2;

import ch.heigvd.ser.labo2.coups.*;
import org.jdom2.Document;
import org.jdom2.Element;

import java.io.*;
import java.lang.reflect.Type;
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
         int noPartie = 1;

         // Pour chaque partie du tournoi.
         for (Element partie : tournoi.getChild("parties").getChildren())
         {
            String resultFilename = tournoi.getAttributeValue("nom") + "_" + (noPartie++) + ".PGN";

            // Création du fichier de sortie.
            PrintWriter out = null;
            try
            {
               out = new PrintWriter(new FileWriter(resultFilename));
            }
            catch (IOException e)
            {
               System.out.println(e.getMessage());
               return;
            }

            int turn = 1;
            boolean shouldTurnChange = false;

            // Pour chaque coup
            for (Element coup : partie.getChild("coups").getChildren())
            {
               // Création du coup spécial
               CoupSpecial cs = getCoupspecialFromString(coup.getAttributeValue("coup_special"));

               // Est-ce un déplacement ou un roque ?
               Element typeCoup = coup.getChild("deplacement");
               Coup objCoup = null;
               if (typeCoup != null) // Le coup est un déplacement
               {
                  try
                  {
                     objCoup = getDeplacementFromElement(typeCoup, cs);
                  }
                  catch (Exception e)
                  {
                     System.out.println(e.getMessage());
                  }
               }
               else // C'est donc un roque
               {
                  typeCoup = coup.getChild("roque");
                  objCoup = getRoqueFromElement(typeCoup, cs);
               }

               String notationPGN = objCoup.notationPGN();


               if (shouldTurnChange) // Coup noir
               {
                  out.println(" " + notationPGN);
                  shouldTurnChange = false;
                  ++turn;
               }
               else // Coup blanc
               {
                  out.print(turn + " " + notationPGN);
                  shouldTurnChange = true;
               }
            }

            out.close();
         }
      }
   }

   private static CoupSpecial getCoupspecialFromString(String readValue)
   {
      if (readValue == null) return null;

      return CoupSpecial.valueOf(readValue.toUpperCase());
   }

   private static TypePiece getTypePieceFromString(String readValue)
   {
      if (readValue == null) return null;

      return TypePiece.valueOf(readValue);
   }

   private static TypeRoque getTypeRoqueFromString(String readValue)
   {
      if (readValue == null) return null;

      return TypeRoque.valueOf(readValue.substring(0,5).toUpperCase());
   }

   private static Case getCaseFromString(String readValue)
   {
      if (readValue == null) return null;

      //readValue.charAt(1)
      return new Case(readValue.charAt(0), Integer.parseInt(readValue.substring(1,2)));
   }

   private static Deplacement getDeplacementFromElement(Element elemDeplacement, CoupSpecial isSpecial) throws Exception
   {
      TypePiece movedPiece = getTypePieceFromString(elemDeplacement.getAttributeValue("piece"));
      TypePiece killedPiece = getTypePieceFromString(elemDeplacement.getAttributeValue("elimination"));
      TypePiece promotedTo = getTypePieceFromString(elemDeplacement.getAttributeValue("promotion"));
      Case depart = getCaseFromString(elemDeplacement.getAttributeValue("case_depart"));
      Case arrivee = getCaseFromString(elemDeplacement.getAttributeValue("case_arrivee"));

      return new Deplacement(movedPiece, killedPiece, promotedTo, isSpecial, depart, arrivee);
   }

   private static Roque getRoqueFromElement(Element elemRoque, CoupSpecial isSpecial)
   {
      TypeRoque tr = getTypeRoqueFromString(elemRoque.getAttributeValue("type"));

      return new Roque(isSpecial, tr);
   }
}