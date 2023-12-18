package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Transactional
    public void saveWithDish(SetmealDTO setmealDTO) {
        // 新建套餐操作类
        Setmeal setmeal = new Setmeal();
        // 拷贝数据
        BeanUtils.copyProperties(setmealDTO, setmeal);

        // 向套餐表插入数据
        setmealMapper.insert(setmeal);

        // 套餐插入后会生成一个套餐id返回
        Long setmealId = setmeal.getId();
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });

        // 保存菜品和套餐的关联关系
        setmealDishMapper.insertBatch(setmealDishes);
    }


    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }


    /**
     * 删除套餐
     * @param ids
     */
    @Transactional
    public void deleteBatch(List<Long> ids) {

        // 起售中的不能删除
        List<Setmeal> setmeals = setmealMapper.getByIds(ids);
        setmeals.forEach(setmeal -> {
            if(setmeal.getStatus() == StatusConstant.ENABLE){
                // 起售中不可删除
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        });

        // 删除套餐
        setmealMapper.deleteByIds(ids);

        // 删除套餐与菜品表中的对应信息
        setmealDishMapper.deleteBySetmealIds(ids);
    }


    /**
     * 根据id查询套餐信息
     * @param id
     * @return
     */
    public SetmealVO getByIdWithDish(Long id) {
        // 获取套餐基本信息
        Setmeal setmeal = setmealMapper.getById(id);

        // 获取套餐所包含的菜品
        List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }


    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    @Transactional
    public void updateWithDish(SetmealDTO setmealDTO) {
        // 修改套餐基本信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);

        // 删除原有套餐关联菜品表的信息
        Long setmealId = setmealDTO.getId();
        setmealDishMapper.deleteBySetmealId(setmealId);

        // 重新写入
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });
        setmealDishMapper.insertBatch(setmealDishes);
    }


    /**
     * 套餐起售停售
     * @param status
     * @param id
     * @return
     */
    public void startOrStop(Integer status, Long id) {
        // 起售套餐的时候检查里面的菜品有没有停售的
        if(status == StatusConstant.ENABLE){
            List<SetmealDish> setmealDishes = setmealDishMapper.getBySetmealId(id);
            setmealDishes.forEach(setmealDish -> {
                Long dishId = setmealDish.getDishId();
                Dish dish = dishMapper.getById(dishId);
                if(dish.getStatus() == StatusConstant.DISABLE) {
                    throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                }
            });
        }

        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }


    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }


    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }
}
