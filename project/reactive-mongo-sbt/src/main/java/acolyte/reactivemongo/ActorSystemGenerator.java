package acolyte.reactivemongo;

import java.io.File;

import javassist.CtNewConstructor;
import javassist.LoaderClassPath;
import javassist.CtNewMethod;
import javassist.ClassPool;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.CtField;
import javassist.CtClass;

import akka.actor.ActorSystem;

/**
 * Actor system generator.
 */
public final class ActorSystemGenerator {
    public static File[] writeTo(File outdir) throws Exception {
        final ClassPool pool = ClassPool.getDefault();
        final LoaderClassPath lcp = 
            new LoaderClassPath(ActorSystem.class.getClassLoader());

        pool.appendClassPath(lcp);

        final CtClass asc = pool.get(ActorSystem.class.getName());
        final CtClass fc = pool.get(ActorRefFactory.class.getName());
        final CtClass pc = 
            pool.makeClass("acolyte.reactivemongo.ActorSystem");

        pc.setSuperclass(asc);

        // New fields
        pc.addField(CtField.make("private final " + ActorSystem.class.getName() + " underlying;", pc));
        pc.addField(CtField.make("private final " + ActorRefFactory.class.getName() + " refFactory;", pc));

        // New constructor
        pc.addConstructor(CtNewConstructor.make(new CtClass[] { asc, fc }, new CtClass[0], "{ if ($1 == null) { throw new IllegalArgumentException(\"Missing underlying system\"); } if ($2 == null) { throw new IllegalArgumentException(\"Missing reference factory\"); } this.underlying = $1; this.refFactory = $2; }", pc));

        final CtMethod[] ms = pc.getMethods();

        for (int i = 0; i < ms.length; i++) {
            if (!ms[i].getDeclaringClass().
                getPackageName().startsWith("akka.")) {

                continue;
            } // end of if

            // ---

            final int mod = Modifier.
                clear(ms[i].getModifiers(), Modifier.ABSTRACT);

            if (Modifier.isStatic(mod) || Modifier.isPrivate(mod) ||
                Modifier.isNative(mod) || Modifier.isFinal(mod)) {

                continue;
            } // end of if

            // ---

            final CtClass returnType = ms[i].getReturnType();
            final String name = ms[i].getName();
            final String body = (returnType == CtClass.voidType)
                ? "{ underlying." + name + "($$); }"
                : !"akka.actor.ActorRef".equals(returnType.getName())
                ? "{ return underlying." + name + "($$); }"
                : "{ final akka.actor.ActorRef then = underlying." + name + 
                "($$); return this.refFactory.before(underlying, then); }";

            pc.addMethod(CtNewMethod.make(mod,
                                          ms[i].getReturnType(),
                                          ms[i].getName(),
                                          ms[i].getParameterTypes(),
                                          ms[i].getExceptionTypes(),
                                          body,
                                          pc));
        } // end of for

        final File packageDir = 
            new File(new File(outdir, "acolyte"), "reactivemongo");

        packageDir.mkdirs();

        final String outpath = outdir.getAbsolutePath();

        fc.writeFile(outpath);
        pc.writeFile(outpath);

        return new File[] {
            new File(packageDir, fc.getSimpleName() + ".class"),
            new File(packageDir, pc.getSimpleName() + ".class")
        };
    } // end of writeTo
} // end of class ActorSystemGenerator
