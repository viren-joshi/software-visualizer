import { Grid2, Box, Divider, Typography, List, ListItemText } from '@mui/material';
import Sidebar from '../sidebar/Sidebar';
import GraphWhiteBoard from '../graphWhiteBoard/GraphWhiteBoard';

export interface ClassContainer {
  classContainer: string;
  userClassList: UserClass[];
  externalDependencyList: string[];
};

export interface UserClass {
  name: string;
  inherits: string;
  classType: string;
  variableList: Variable[];
  methodList: Method[];
  isNested: boolean;
  isControllerClass: boolean;
  nestedClassesList: UserClass[];
  annotations: string[];
  implementationList: string[];
}

export interface Variable {
  identifier: string;
  datatype: string;
  annotationList: string[];
  isStatic: boolean;
  isAnnotated: boolean;
}

export interface Method {
  methodName: string;
  annotations: string[];
  isStatic: boolean;
}

function Main() {
  //dummy api response
  const data: ClassContainer = {
    "classContainer": "",
    "userClassList": [
      {
        "name": "com.blog.BlogApplication",
        "inherits": "",
        "classType": "normalClass",
        "variableList": [],
        "methodList": [
          {
            "methodName": "main",
            "annotations": [],
            "isStatic": true
          }
        ],
        "isNested": false,
        "isControllerClass": false,
        "nestedClassesList": [],
        "annotations": [
          "@org.springframework.boot.autoconfigure.SpringBootApplication"
        ],
        "implementationList": []
      },
      {
        "name": "com.blog.content.entity.Post",
        "inherits": "",
        "classType": "normalClass",
        "variableList": [
          {
            "identifier": "id",
            "datatype": "java.lang.Long",
            "annotationList": [
              "@jakarta.persistence.Id",
              "@jakarta.persistence.GeneratedValue(strategy=jakarta.persistence.GenerationType.IDENTITY)"
            ],
            "isStatic": false,
            "isAnnotated": true
          },
          {
            "identifier": "title",
            "datatype": "java.lang.String",
            "annotationList": [],
            "isStatic": false,
            "isAnnotated": false
          },
          {
            "identifier": "content",
            "datatype": "java.lang.String",
            "annotationList": [],
            "isStatic": false,
            "isAnnotated": false
          },
          {
            "identifier": "createdAt",
            "datatype": "java.util.Date",
            "annotationList": [],
            "isStatic": false,
            "isAnnotated": false
          },
          {
            "identifier": "updatedAt",
            "datatype": "java.util.Date",
            "annotationList": [],
            "isStatic": false,
            "isAnnotated": false
          },
          {
            "identifier": "author",
            "datatype": "com.blog.user.entity.User",
            "annotationList": [
              "@jakarta.persistence.ManyToOne",
              "@jakarta.persistence.JoinColumn(name=\"author_id\", nullable=false)"
            ],
            "isStatic": false,
            "isAnnotated": true
          }
        ],
        "methodList": [
          {
            "methodName": "getId",
            "annotations": [],
            "isStatic": false
          },
          {
            "methodName": "setId",
            "annotations": [],
            "isStatic": false
          },
          {
            "methodName": "getTitle",
            "annotations": [],
            "isStatic": false
          },
          {
            "methodName": "setTitle",
            "annotations": [],
            "isStatic": false
          },
          {
            "methodName": "getContent",
            "annotations": [],
            "isStatic": false
          },
          {
            "methodName": "setContent",
            "annotations": [],
            "isStatic": false
          },
          {
            "methodName": "getCreatedAt",
            "annotations": [],
            "isStatic": false
          },
          {
            "methodName": "setCreatedAt",
            "annotations": [],
            "isStatic": false
          },
          {
            "methodName": "getUpdatedAt",
            "annotations": [],
            "isStatic": false
          },
          {
            "methodName": "setUpdatedAt",
            "annotations": [],
            "isStatic": false
          }
        ],
        "isNested": false,
        "isControllerClass": false,
        "nestedClassesList": [],
        "annotations": [
          "@jakarta.persistence.Entity"
        ],
        "implementationList": []
      },
      {
        "name": "com.blog.content.controller.B",
        "inherits": "",
        "classType": "normalClass",
        "variableList": [],
        "methodList": [],
        "isNested": false,
        "isControllerClass": false,
        "nestedClassesList": [],
        "annotations": [],
        "implementationList": []
      },
      {
        "name": "com.blog.content.controller.A",
        "inherits": "com.blog.content.controller.B",
        "classType": "normalClass",
        "variableList": [],
        "methodList": [],
        "isNested": false,
        "isControllerClass": false,
        "nestedClassesList": [],
        "annotations": [],
        "implementationList": []
      },
      {
        "name": "com.blog.user.repository.UserRepository",
        "inherits": "",
        "classType": "interfaceClass",
        "variableList": [],
        "methodList": [],
        "isNested": false,
        "isControllerClass": false,
        "nestedClassesList": [],
        "annotations": [],
        "implementationList": []
      },
      {
        "name": "com.blog.user.entity.User",
        "inherits": "",
        "classType": "normalClass",
        "variableList": [
          {
            "identifier": "id",
            "datatype": "java.lang.Long",
            "annotationList": [
              "@jakarta.persistence.Id",
              "@jakarta.persistence.GeneratedValue(strategy=jakarta.persistence.GenerationType.IDENTITY)"
            ],
            "isStatic": false,
            "isAnnotated": true
          },
          {
            "identifier": "username",
            "datatype": "java.lang.String",
            "annotationList": [],
            "isStatic": false,
            "isAnnotated": false
          },
          {
            "identifier": "password",
            "datatype": "java.lang.String",
            "annotationList": [],
            "isStatic": false,
            "isAnnotated": false
          },
          {
            "identifier": "email",
            "datatype": "java.lang.String",
            "annotationList": [],
            "isStatic": false,
            "isAnnotated": false
          },
          {
            "identifier": "posts",
            "datatype": "java.util.List",
            "annotationList": [
              "@jakarta.persistence.OneToMany(mappedBy=\"author\", cascade={jakarta.persistence.CascadeType.ALL})"
            ],
            "isStatic": false,
            "isAnnotated": true
          }
        ],
        "methodList": [
          {
            "methodName": "getUsername",
            "annotations": [],
            "isStatic": false
          },
          {
            "methodName": "setUsername",
            "annotations": [],
            "isStatic": false
          },
          {
            "methodName": "getPassword",
            "annotations": [],
            "isStatic": false
          },
          {
            "methodName": "setPassword",
            "annotations": [],
            "isStatic": false
          },
          {
            "methodName": "getEmail",
            "annotations": [],
            "isStatic": false
          },
          {
            "methodName": "setEmail",
            "annotations": [],
            "isStatic": false
          },
          {
            "methodName": "getId",
            "annotations": [],
            "isStatic": false
          },
          {
            "methodName": "setId",
            "annotations": [],
            "isStatic": false
          },
          {
            "methodName": "getPosts",
            "annotations": [],
            "isStatic": false
          },
          {
            "methodName": "setPosts",
            "annotations": [],
            "isStatic": false
          }
        ],
        "isNested": false,
        "isControllerClass": false,
        "nestedClassesList": [],
        "annotations": [
          "@jakarta.persistence.Entity"
        ],
        "implementationList": []
      },
      {
        "name": "com.blog.user.controller.UserController",
        "inherits": "",
        "classType": "normalClass",
        "variableList": [
          {
            "identifier": "userService",
            "datatype": "com.blog.user.service.UserService",
            "annotationList": [],
            "isStatic": false,
            "isAnnotated": false
          }
        ],
        "methodList": [
          {
            "methodName": "registerUser",
            "annotations": [
              "@org.springframework.web.bind.annotation.PostMapping(value={\"/register\"})"
            ],
            "isStatic": false
          }
        ],
        "isNested": false,
        "isControllerClass": false,
        "nestedClassesList": [],
        "annotations": [
          "@org.springframework.web.bind.annotation.RestController",
          "@org.springframework.web.bind.annotation.RequestMapping(value={\"/users\"})"
        ],
        "implementationList": []
      },
      {
        "name": "com.blog.user.service.UserService",
        "inherits": "",
        "classType": "normalClass",
        "variableList": [
          {
            "identifier": "userRepository",
            "datatype": "com.blog.user.repository.UserRepository",
            "annotationList": [],
            "isStatic": false,
            "isAnnotated": false
          }
        ],
        "methodList": [
          {
            "methodName": "createUser",
            "annotations": [],
            "isStatic": false
          },
          {
            "methodName": "generateClassDependencyGraph",
            "annotations": [],
            "isStatic": false
          }
        ],
        "isNested": false,
        "isControllerClass": false,
        "nestedClassesList": [],
        "annotations": [
          "@org.springframework.stereotype.Service"
        ],
        "implementationList": []
      }
    ],
    "externalDependencyList": []
  };
  const classNames = data.userClassList.map((userClass) => userClass.name);
  return (
    <Grid2 container spacing={2} sx={{ height: '100vh' }}>
      <Grid2 size={3}>
        <Box
          sx={{
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            padding: '20px',
          }}
        >
          <Sidebar classNames={classNames}/>
        </Box>
      </Grid2>

      {/* Vertical Divider */}
      <Divider orientation="vertical" flexItem  sx={{ borderColor: 'black' }}/>

      {/* Right part */}
      <Grid2 size={6}>
        <Box
          sx={{
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
            padding: '20px',
          }}
        >
          <GraphWhiteBoard/>
        </Box>
      </Grid2>
    </Grid2>
  );
}

export default Main
