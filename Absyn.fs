(* File MicroC/Absyn.fs
   Abstract syntax of micro-C, an imperative language.
   sestoft@itu.dk 2009-09-25

   Must precede Interp.fs, Comp.fs and Contcomp.fs in Solution Explorer
 *)

module Absyn

// 基本类型
// 注意，数组、指针是递归类型
// 这里没有函数类型，注意与上次课的 MicroML 对比
type typ =
  | TypI                                (* Type int                    *)
  | TypC                                (* Type char                   *)           
  | TypFloat                            (* Type flaot                  *)
  | TypString                           (* Type string                 *)
  | TypA of typ * int option            (* Array type                  *)
  | TypP of typ                         (* Pointer type                *)
                                                                   
and expr =                              // 表达式，右值                                                
  | Access of access                    (* x    or  *p    or  a[e]     *) //访问左值（右值）
  | Assign of access * expr             (* x=e  or  *p=e  or  a[e]=e   *)
  | Addr of access                      (* &x   or  &*p   or  &a[e]    *)
  | CstI of int                         (* Constant                    *)
  | ConstFloat of float32               (*constant float*)  
  | ConstChar of char                   (*constant char*) 
  | ConstString of string               (*constant string*) 
  | Prim1 of string * expr              (* Unary primitive operator 单  *)
  | Prim2 of string * expr * expr       (* Binary primitive operator 双 *)
  | Prim3 of expr * expr * expr         (* 三目运算符                   *)    
  | AndOperator of expr * expr          (* Sequential and               *)
  | OrOperator of expr * expr           (* Sequential or                *)
  | CallOperator of string * expr list  (* Function call f(...)         *)
  | Printf of string * expr
                                                                   
and access =                         //左值，存储的位置                                            
  | AccVar of string                 (* Variable access        x    *) 
  | AccDeref of expr                 (* Pointer dereferencing  *p   *)
  | AccIndex of access * expr        (* Array indexing         a[e] *)
                                                                   
and stmt =                                                         
  | If of expr * stmt * stmt         (* Conditional                 *)
  | Switch of expr * stmt list
  | Case of expr * stmt
  | While of expr * stmt             (* While loop                  *)
  | Dowhile of stmt * expr           (* Dowhile loop                  *)
  | For of expr * expr * expr * stmt (* For loop                    *)
  | Expr of expr                     (* Expression statement   e;   *)
  | Return of expr option            (* Return from method          *)
  | Block of stmtordec list          (* Block: grouping and scope   *)
  | Break
  | Default of stmt
  | Continue
  | Try of stmt * stmt list
  | Catch of IException * stmt
  | Finally of stmt
  | Throw of expr
  | Expression of expr

and IException = 
    | Exception of string  
  // 语句块内部，可以是变量声明 或语句的列表                                                              

  //本地变量声明
and stmtordec =                                                    
  | Dec of typ * string              (* Local variable declaration  *)
  | DeclareAndAssign of typ * string * expr  (*声明变量并定义*)
  | Stmt of stmt                     (* A statement                 *)

// 顶级声明 可以是函数声明或变量声明
and topdec = 
  | Fundec of typ option * string * (typ * string) list * stmt
  | Vardec of typ * string
  | VariableDeclareAndAssign of typ * string * expr (*声明变量并定义*)

// 程序是顶级声明的列表
and program = 
  | Prog of topdec list
