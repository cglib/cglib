<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<HTML>
  <HEAD>
    <TITLE></TITLE>
  </HEAD>
  <BODY LEFTMARGIN="10" RIGHTMARGIN="0" TOPMARGIN="10"
        BOTTOMMARGIN="10" MARGINHEIGHT="0" MARGINWIDTH="0"
                alink='navy' vlink='navy' link='navy'
                style='background-color:a0c0fe' 
       >
 <table cellspacing='0' border='0'>
 <tr>
  <td colspan='5'><A href="http://sourceforge.net"> 
      <IMG src="http://sourceforge.net/sflogo.php?group_id=56933&amp;type=5" width="210" height="62" border="0" alt="SourceForge Logo">
      </A>
      <p> 
  </td>
  
 </tr> 
 <tr> 
   <td valign="top" >
     <H5> Project </h5>

       <li><a href='http://sourceforge.net/projects/cglib'> Summary   </a>
       <li><a href='http://sourceforge.net/cvs/?group_id=56933'> CVS  </a>
       <li><a href='http://sourceforge.net/project/showfiles.php?group_id=56933'>
                        Downloads  </a>
       <li><a href='http://sourceforge.net/forum/?group_id=56933'> Forum </a>
       
       
     <H5> Documentation </h5>
   <!-- Links on the left -->
      <li><a  href='api/index.html'> API  </a>
     <H5> Samples </h5>
   
      <li><a href='Trace.html'> Trace  </a>
      <li><a href='Beans.html'> Beans  </a>


     <H5> Links </h5>
      <li> <a  href='http://jakarta.apache.org/bcel'> BCEL  </a>
   </td>
   <td width='5%' >&nbsp;</td>
   <td width='10%' style='background-color:ffffff'>&nbsp;</td>
   <td  valign='top' style='background-color:ffffff'>
     
   <!-- Content   -->
       <br>
             <h3 align='center'> Code Generation Library  </h3>
       <br>
      
       <p>
        CGLIB outputs generated classes. Byte code generation and class file
        format manipulation is hidden in static methods.
        It was designed to implement Transparent Persistence for JAVA, but
        can be used to implement aspects like Security or Validation.
       <p> Simple example :
       Implement <a href='api/net/sf/cglib/proxy/MethodInterceptor.html'>MethodInterceptor</a>
       <pre >
<b>public class </b>Trace <b>implements</b> MethodInterceptor{

   <b>private static</b> Trace trace = <b>new</b> Trace();
    
   <b>public boolean invokeSuper</b>( Object obj, Method method,
                        Object args[])<b> throws </b> Throwable{
<span style="color:green">//invoke supper in generated code</span>
         <b><span style="color:red">System.out.println("before: " +  method )</span></b>;
      <b>return true </b>; 
     }
   <b>public</b> Object <b>afterReturn</b>(
                             Object obj, Method method,
                             Object args[],<b>boolean</b> invokedSuper, 
                             Object retValFromSuper,Throwable e 
                                   )<b>throws</b> Throwable{
<span style="color:green">//print "this" and method signature  </span>
            <b><span style="color:red">System.out.println("after: " +  method )</span></b>;
       <b>return</b> retValFromSuper;
     }
 <b>public static</b> Object <b>newInstance</b>(Class clazz){
<span style="color:green">//generate code</span>
        <b>return <span style="color:red">Enhancer.enhance(clazz, null, trace)</span></b>;
   }
 }   
 </pre>

 Use static method to instantiate object:
<pre>
 List list = (List)Trace.<b>newInstance</b>( Vector.<b>class</b> );
 list.add( "TEST" );    
</pre> 
  This code must produce output like this:<br>
 
  <span style="color:green">
  before: public boolean Vector.add( Object ) <br>
  after: public boolean Vector.add( Object ) <br>
      
  </span>
   <br>
   <br>
   </td>
   <td width='10%' style='background-color:ffffff'>&nbsp;</td>
   <td width='5%' >&nbsp;</td>
 </tr>
 <table>
  </BODY>
</HTML>
