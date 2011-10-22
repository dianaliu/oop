#include "java_lang.h"

//the data layout of Test
typedef __Test* Test;

struct __Test {
	__Test_VT* __vptr; 
	int i = 1;
	
	static int32_t hashCode(Test);
	static bool equals(Test, Object);
	static Class getClass(Test);
	static String toString(Test);
	static int meth1(Test,int);
	static int meth2(Test,int);
	static int meth3(Test,int);
	
	static Class __class();
	
	static __Test_VT __vtable;
}

struct __Test_VT {
	Class __isa;
	int32_t (*hashCode)(Test);
	bool (*equals)(Test, Object);
	Class (*getClass)(Test);
	String (*toString)(Test);
	int (*meth1)(Test,int);
	int (*meth2)(Test,int);
	int (*meth3)(Test,int);
	
	__Object_VT()
	: __isa(__Test::__class()),
	hashCode(&__Object::hashCode),
	equals(&__Test ::equals),
	getClass(&__Test::getClass),
	toString(&__Test::toString),
	meth1(&__Test::meth1),
	meth2(&__Test::meth2),
	meth3(&__Test::meth3) {
	}
}