package com.example.demo.employee.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.example.demo.employee.dto.DepartmentDTO;
import com.example.demo.employee.dto.DeptOrganizationDTO;
import com.example.demo.employee.dto.EmployeeDTO;
import com.example.demo.employee.dto.HobbyDTO;
import com.example.demo.employee.service.EmployeeService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import oracle.sql.json.OracleJsonArray;

@Controller
@RequiredArgsConstructor
public class EmployeeController {
	
	private final EmployeeService employeeservice;
	
	//main page
	@GetMapping("/employeeControl")
	public String employeeControl() {
		return "employee/employeeControlView";
	}

	//organization
	@GetMapping("/departmentOrganization")
		   public String departmentControl(Model model) {
		      List<DeptOrganizationDTO> deptOrganizationList = employeeservice.getEmpDept();      
		      //Map<String, Object> data = new HashMap<String, Object>();
		      String json = null;
		      ModelAndView mav = new ModelAndView();
		      JSONArray Ljarray = new JSONArray();
		      
		      ArrayList<JSONObject> work = new ArrayList<JSONObject>();
		      try {
		         for (DeptOrganizationDTO dto : deptOrganizationList) {
		            JSONObject jo1 = new JSONObject();
		            JSONObject jo2 = new JSONObject();
		            JSONObject jo3 = new JSONObject();
		            JSONArray jarray = new JSONArray();
		           
		            jo1.put("v", dto.getDeptId());
		            String f = dto.getDeptNm() + "," + (dto.getEmpList() + "@");
		            jo1.put("f", f);
		            jo2.put("", dto.getUpdeptId());
		            jo3.put("", dto.getDeptNm());
		            jarray.add(jo1);
		            jarray.add(jo2);
		            jarray.add(jo3);
		            Ljarray.add(jarray);
		            //System.out.println(Ljarray);
		         }

		         json = new ObjectMapper().writeValueAsString(Ljarray);
		         json = json.replace("\"\":", "");
		         json = json.replace(",{", ",");
		         json = json.replace("}", "");
		         json = json.replace("@\"", "\"}");
		         System.out.println(json);  

//		         mav.addObject("myjsonarr",json);
//		         mav.setViewName("employee/departmentOrganization.html");
		         model.addAttribute("myjsonarr",json);
		         return "employee/departmentOrganization";
		      } catch (Exception e) {
		         // TODO: handle exception
		      }
		      return "";
		   }
		   
		   private char[] type(String json) {
		      // TODO Auto-generated method stub
		      return null;
		   }

	//
	@GetMapping("/employees/new")
	public String createForm(Model model) {
		//
		List<DepartmentDTO> deptList = employeeservice.selectDepartment();
		model.addAttribute("deptlist", deptList);
		//
		List<HobbyDTO> hobbyList = employeeservice.selectHobby();
		model.addAttribute("hobbylist", hobbyList);
		return "employee/employeeCreateView";
	}
	
	//
		@PostMapping("/employees/new")
		public String insertEmployee(@Valid EmployeeDTO dto, Errors errors, Model model) {
			//
			if (errors.hasErrors()) {
				Map<String, String> validatorResult = employeeservice.validateHandling(errors);
				for (String key : validatorResult.keySet()) {
					model.addAttribute(key, validatorResult.get(key));
				}
				//
				List<DepartmentDTO> deptList = employeeservice.selectDepartment();
				model.addAttribute("deptlist", deptList);
				//
				List<HobbyDTO> hobbyList = employeeservice.selectHobby();
				model.addAttribute("hobbylist", hobbyList);
				return "employee/employeeCreateView";
			} else {
				//
				String Msg = "ENROLL COMPLETE";
				if (employeeservice.idDuplicateCheck(dto) >= 1) {
					Msg = "ID DUPLICATE. TRY AGAIN.";
					model.addAttribute("msg", Msg);
				} else {
					//
					employeeservice.create(dto);
					//
					if (dto.getHobbies() != null) {
						String[] emphobarr = dto.getHobbies().split(",");
						for (int i = 0; i < emphobarr.length; i++) {
							employeeservice.insertEmpHob(emphobarr[i], dto);
						}
					}
					model.addAttribute("msg",Msg);
				}
				//
				//
				List<DepartmentDTO> deptList = employeeservice.selectDepartment();
				model.addAttribute("deptlist", deptList);
				//
				List<HobbyDTO> hobbyList = employeeservice.selectHobby();
				model.addAttribute("hobbylist", hobbyList);
				return "employee/employeeCreateView";				
			}
		}
		
	//
	@GetMapping("/employees/search")
	public String searchEmployee(EmployeeDTO dto, Model model) {
		List<EmployeeDTO> employeeList = employeeservice.search(dto);
		model.addAttribute("employeelist", employeeList);
		return "employee/employeeControlView";
	}
	
	//
	@GetMapping("/employees/update/{id}")
	public String updateEmployee(@PathVariable String id, Model model) {
		//
		EmployeeDTO selectedEmployee = employeeservice.selectOne(id);
		model.addAttribute("selectedemployee", selectedEmployee);
		//
		DepartmentDTO selDept = employeeservice.selectedDepartment(id);
		model.addAttribute("seldept", selDept);
		//
		String seldeptid = selDept.getDeptId();
		List<DepartmentDTO> notselDeptList = employeeservice.notselectedDepartment(seldeptid);
		model.addAttribute("notseldeptlist", notselDeptList);
		//
		List<HobbyDTO> selHobbyList = employeeservice.selectedHobby(id);
		model.addAttribute("selhobbylist", selHobbyList);
		//
		//
		List<HobbyDTO> notselHobbyList = new ArrayList<HobbyDTO>();
		String id1, id2, id3, id4, id5;
		if (selHobbyList.size() == 1) {//
			id1 = selHobbyList.get(0).getHobId();
			notselHobbyList = employeeservice.notselectedHobby1(id1);
			model.addAttribute("notselhobbylist", notselHobbyList);
		} else if (selHobbyList.size() == 2) {//
			id1 = selHobbyList.get(0).getHobId();
			id2 = selHobbyList.get(1).getHobId();			
			notselHobbyList = employeeservice.notselectedHobby2(id1, id2);
			model.addAttribute("notselhobbylist", notselHobbyList);
		} else if (selHobbyList.size() == 3) {//
			id1 = selHobbyList.get(0).getHobId();
			id2 = selHobbyList.get(1).getHobId();
			id3 = selHobbyList.get(2).getHobId();
			notselHobbyList = employeeservice.notselectedHobby3(id1, id2, id3);
			model.addAttribute("notselhobbylist", notselHobbyList);
		} else if (selHobbyList.size() == 4) {//
			id1 = selHobbyList.get(0).getHobId();
			id2 = selHobbyList.get(1).getHobId();
			id3 = selHobbyList.get(2).getHobId();
			id4 = selHobbyList.get(3).getHobId();			
			notselHobbyList = employeeservice.notselectedHobby4(id1, id2, id3, id4);
			model.addAttribute("notselhobbylist", notselHobbyList);
		} else if (selHobbyList.size() == 5) {//
			id1 = selHobbyList.get(0).getHobId();
			id2 = selHobbyList.get(1).getHobId();
			id3 = selHobbyList.get(2).getHobId();
			id4 = selHobbyList.get(3).getHobId();
			id5 = selHobbyList.get(4).getHobId();			
			notselHobbyList = employeeservice.notselectedHobby5(id1, id2, id3, id4, id5);
			model.addAttribute("notselhobbylist", notselHobbyList);
		} else if (selHobbyList.size() == 0) {//
			notselHobbyList = employeeservice.notselectedHobby0();
			model.addAttribute("notselhobbylist", notselHobbyList);
		}
		return "employee/employeeUpdateView";
	}
	
	//
	@PutMapping("/employees/{id}")
	public String updateEmployee(@PathVariable String id, EmployeeDTO dto) {
		//
		employeeservice.updateEmp(id, dto);
		// 
		employeeservice.deleteEmpHob(id);
		if (dto.getHobbies() != null) {
			String[] emphobarr = dto.getHobbies().split(",");
			for (int i = 0; i < emphobarr.length; i++) {
				employeeservice.updateEmpHob(id, emphobarr[i]);
			}			
		}
		return "employee/employeeControlView";
	}
	
	//
	@DeleteMapping("/employees/{id}")
	public String deleteEmployee(@PathVariable String id) {
		employeeservice.delete(id);
		return "employee/employeeControlView";
	}
	
	//select all
	//	@GetMapping("/employees")
	//	public String getEmployees(Model model) {
	//		List<EmployeeDTO> employeeList = employeeservice.select(); 	
	//		model.addAttribute("employeeList", employeeList);
	//		return "employee/employeeListView";
	//	}
	
	//select one
	@GetMapping("/employees/{id}")
	public String getEmployee(@PathVariable String id, Model model) {
		EmployeeDTO selectedEmployee = employeeservice.selectOne(id);
		model.addAttribute("employeelist", selectedEmployee);
		return "employee/employeeControlView";
	}
}
