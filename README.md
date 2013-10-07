uima_gimli
==========

Gimli UIMA Annotator


Gimli is a library and tool for high-performance and multi-corpus recognition of biomedical entity names.
See https://github.com/davidcampos/gimli

UIMA is an open framework for building analytic applications - to find latent meaning, relationships and relevant facts hidden in unstructured text. 
See http://uima.apache.org/

*You need to unzip resources/tools/gdep.zip first.*

Install this artifact into your local repository `mvn install` and reference it in other projects like this:

     <dependency>
       <groupId>ch.epfl.bbp</groupId>
       <artifactId>uima_gimli</artifactId>
       <version>1.0.2</version>
     </dependency>

TODOes:
- even more robust matching of Gimli annotations to UIMA annotations
