// This is the correct translation, done manually.

#include <iostream>
#include "ptr.h"
#include "java_lang.h"

namespace java {
  namespace lang {

    // Forward declaration of datalayout and vt
    struct __EasyTest;
    struct __EasyTest_VT;

    typedef __rt::Ptr<__EasyTest> EasyTest;

    // The data layout for EasyTest
    struct __EasyTest {
        __EasyTest_VT* __vptr;

        int32_t field;
        // Constructor
        __EasyTest();
        // Destructor
        static void __delete(__EasyTest*);

        static int32_t hashCode(EasyTest);
        static bool equals(EasyTest, Object);
        static Class getClass(EasyTest);
        static String toString(EasyTest);
        static int32_t number(EasyTest);
        static void setNumber(EasyTest, int32_t);
        static __EasyTest_VT __vtable;

      static Class __class();

    };

    // The vtable layout for EasyTest
    struct __EasyTest_VT {
        Class __isa;
        void (*__delete)(__EasyTest*);
        int32_t (*hashCode)(EasyTest);
        bool (*equals)(EasyTest, Object);
        Class (*getClass)(EasyTest);
        String (*toString)(EasyTest);
        int32_t (*number)(EasyTest);
        void (*setNumber)(EasyTest, int32_t);
        
        __EasyTest_VT():           
           __isa(__Object::__class()), 
          __delete((void(*)(__EasyTest*))&__Object::__delete), 
          hashCode((int32_t(*)(EasyTest))&__Object::hashCode), 
          equals((bool(*)(EasyTest, Object))&__Object::equals), 
          getClass((Class(*)(EasyTest))&__Object::getClass), 
          toString((String(*)(EasyTest))&__Object::toString), 
          number(&__EasyTest::number), 
          setNumber(&__EasyTest::setNumber) {
        }
    };

    // The destructor
    void __EasyTest::__delete(__EasyTest* __this) {
      delete __this;
    }

    // Empty constructors for class and vt
      __EasyTest::__EasyTest() :  __vptr(&__vtable) {
      }

      __EasyTest_VT __EasyTest::__vtable;
  }
}


namespace __rt {
}

// ------------ begin CC file --------------
// #include <iostream>
// #include "java_lang.h"
using namespace java::lang;

int32_t __EasyTest::number (EasyTest __this) {
  return __this->field;
}

void __EasyTest::setNumber (EasyTest __this, int32_t newNum) {
   //__rt::checkNotNull(__this->field);
   //__rt::checkNotNull(newNum);
//	newNum);
	__this->field = newNum;
}

int32_t main() {
  EasyTest thing1 = new __EasyTest();
   __rt::checkNotNull(thing1);

  thing1->__vptr->setNumber(thing1, 3);

  EasyTest thing2 = new __EasyTest();
   __rt::checkNotNull(thing2);

  thing2->__vptr->setNumber(thing2, 1);

  EasyTest thing3 = new __EasyTest();
   __rt::checkNotNull(thing3);

  thing3->__vptr->setNumber(thing3, 3339);
  std::cout
     << __rt::literal("It works!")
     << std::endl;
   __rt::checkNotNull(thing1);
  std::cout
     << thing1->__vptr->number(thing1)
     << std::endl;
   __rt::checkNotNull(thing2);
  std::cout
     << thing2->__vptr->number(thing2)
     << std::endl;
   __rt::checkNotNull(thing3);
  std::cout
     << thing3->__vptr->number(thing3)
     << std::endl;
}
